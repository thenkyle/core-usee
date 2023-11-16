package com.thsrc.milvus.services;

import com.thsrc.milvus.utils.PushMaterialConfig;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.partition.CreatePartitionParam;
import io.milvus.response.SearchResultsWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class MilvusService {
    private final MilvusClient milvusClient;

    Logger logger = LogManager.getLogger(getClass());

    static final IndexType INDEX_TYPE = IndexType.IVF_FLAT;
    static final String INDEX_PARAM = "{\"nlist\":1024}";
    private static final String[] ACTION = {"createPNR", "modifyPNR", "payment" };
    private static final String[] STATIONS = {"NAK", "TPE", "BAC", "TAY", "HSC", "MIL", "TAC", "CHA", "TUL", "CHY", "TNN", "ZUY", "PTN"};
    private static final String[] PROFILES = {"F", "H", "E", "W", "P", "T", "S", "M"};


    @Autowired
    public MilvusService(MilvusClient milvusClient) {
        this.milvusClient = milvusClient;
    }

    /** 初始化Milvus*/
    public void initializeMilvus() {
        CreateCollectionParam createCollectionReq = this.createCollectionReq();
        CreatePartitionParam createPartitionReq = this.createPartitionReq();
        R<Boolean> respHasCollection = milvusClient.hasCollection(
                HasCollectionParam.newBuilder()
                        .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                        .build()
        );
        if (respHasCollection.getData().equals(Boolean.TRUE)) {
            System.out.println("Collection exists.");
        } else {
            this.createCollection(createCollectionReq);
            this.createPartition(createPartitionReq);
        }

    }

    public void insertData() {
        insertData((MilvusServiceClient) milvusClient);
    }

    @Async
    public void buildIndex() {
        buildIndex((MilvusServiceClient) milvusClient);
    }
    @Async
    public void loadCollection() {
        loadCollection((MilvusServiceClient) milvusClient);
    }

    public void search() {
        final Integer SEARCH_K = 3;
        final String SEARCH_PARAM = "{\"nprobe\":10}";
        List<String> search_output_fields = Arrays.asList(PushMaterialConfig.Field.CASE_ID);
        List<List<Float>> search_vectors = Arrays.asList(Arrays.asList(0.1f, 0.2f, 0.3f));

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                .withMetricType(MetricType.L2)
                .withOutFields(search_output_fields)
                .withTopK(SEARCH_K)
                .withVectors(search_vectors)
                .withVectorFieldName(PushMaterialConfig.Field.CASE_VECTOR)
                .withParams(SEARCH_PARAM)
                .build();

        R<SearchResults> respSearch = milvusClient.search(searchParam);
        SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(respSearch.getData().getResults());
        System.out.println("wrapperSearch.getIDScore" + wrapperSearch.getIDScore(0));
        System.out.println("wrapperSearch.getFieldData" + wrapperSearch.getFieldData(PushMaterialConfig.Field.CASE_ID, 0));
        // 釋放Collection
//        milvusClient.releaseCollection(
//                ReleaseCollectionParam.newBuilder()
//                        .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
//                        .build());
    }

    /**建立Collection所需參數*/
    private CreateCollectionParam createCollectionReq() {
        FieldType fieldType1 = FieldType.newBuilder()
                .withName(PushMaterialConfig.Field.CASE_ID)
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();
        FieldType fieldType2 = FieldType.newBuilder()
                .withName(PushMaterialConfig.Field.ACTION)
                .withDataType(DataType.VarChar)
                .withDataType(DataType.VarChar).withMaxLength(30)

                .build();
        FieldType fieldType3 = FieldType.newBuilder()
                .withName(PushMaterialConfig.Field.DEPARTRUE_ARRIVE)
                .withDataType(DataType.VarChar).withMaxLength(21)
                .build();
        FieldType fieldType4 = FieldType.newBuilder()
                .withName(PushMaterialConfig.Field.PROFILE)
                .withDataType(DataType.VarChar).withMaxLength(21)
                .build();
        FieldType fieldType5 = FieldType.newBuilder()
                .withName(PushMaterialConfig.Field.CASE_VECTOR)
                .withDataType(DataType.FloatVector)
                .withDimension(3)
                .build();

        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                .withDescription(PushMaterialConfig.DESCRIPTION)
                .withShardsNum(2)
                .addFieldType(fieldType1)
                .addFieldType(fieldType2)
                .addFieldType(fieldType3)
                .addFieldType(fieldType4)
                .addFieldType(fieldType5)
                .build();

        return createCollectionReq;
    }

    /**建立Partition所需參數*/
    private CreatePartitionParam createPartitionReq() {
        CreatePartitionParam createPartitionReq = CreatePartitionParam.newBuilder()
                .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                .withPartitionName(PushMaterialConfig.PARTITION_NAME)
                .build();
        return createPartitionReq;
    }
    /**產生一個Collection*/
    private void createCollection(CreateCollectionParam createCollectionReq) {
    milvusClient.createCollection(createCollectionReq);
        System.out.println("Create Collection Complete.");
    }
    /**產生一個Partition*/
    private void createPartition(CreatePartitionParam createPartitionParamReq){
        milvusClient.createPartition(createPartitionParamReq);
        System.out.println("Create Partition Complete.");
    }

    private void insertData(MilvusServiceClient milvusClient) {
        Random ran = new Random();
        List<Long> case_id_array = new ArrayList<>();
        List<String> action_array = new ArrayList<>();
        List<String> departure_arrive_array = new ArrayList<>(); // 新增 departure_arrive 字段
        List<String> profile_array = new ArrayList<>();
        List<List<Float>> case_vector_array = new ArrayList<>();

        // 生成所有可能的 departure_arrive 組合
        List<String> stationCombinations = generateCombinations(STATIONS);
        List<String> profileCombinations = generateRandomProfiles(PROFILES);

        for (long i = 0L; i < 10; ++i) {
            case_id_array.add(i);
            // 隨機選擇一個 action
            String action = ACTION[ran.nextInt(ACTION.length)];
            action_array.add(action);
            // 隨機選擇一個 departure_arrive 組合
            String departureArrive = stationCombinations.get(ran.nextInt(stationCombinations.size()));
            departure_arrive_array.add(departureArrive);
            String profiles = profileCombinations.get(ran.nextInt(profileCombinations.size()));
            profile_array.add(profiles);

            List<Float> vector = new ArrayList<>();
            for (int k = 0; k < 3; ++k) {
                vector.add(ran.nextFloat());
            }
            case_vector_array.add(vector);
        }

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(PushMaterialConfig.Field.CASE_ID,  case_id_array));
        fields.add(new InsertParam.Field(PushMaterialConfig.Field.ACTION, action_array));
        fields.add(new InsertParam.Field(PushMaterialConfig.Field.DEPARTRUE_ARRIVE, departure_arrive_array));
        fields.add(new InsertParam.Field(PushMaterialConfig.Field.PROFILE, profile_array));
        fields.add(new InsertParam.Field(PushMaterialConfig.Field.CASE_VECTOR, case_vector_array));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                .withPartitionName(PushMaterialConfig.PARTITION_NAME)
                .withFields(fields)
                .build();
        logger.info(insertParam);
        milvusClient.insert(insertParam);
        System.out.println("Insert Data Complete.");

    }
    /** 隨機產生departure_arrive站組合*/
    private List<String> generateCombinations(String[] stations) {
        List<String> combinations = new ArrayList<>();

        for (int i = 0; i < stations.length - 1; i++) {
            for (int j = i + 1; j < stations.length; j++) {
                String combination = stations[i] + stations[j];
                combinations.add(combination);
            }
        }

        return combinations;
    }
    /** 隨機生成profile組合*/
    private static List<String> generateRandomProfiles(String[] count) {
        Random random = new Random();
        List<String> profiles = new ArrayList<>();

        for (int i = 0; i < count.length; ++i) {
            int countDigit1 = random.nextInt(3) + 1;  // 1到3之間的數字
            String letter1 = PROFILES[random.nextInt(PROFILES.length)];
            int countDigit2 = random.nextInt(3) + 1;  // 1到3之間的數字
            String letter2 = PROFILES[random.nextInt(PROFILES.length)];

            String profile = countDigit1 + letter1 + countDigit2 + letter2;
            profiles.add(profile);
        }
        return profiles;
    }

    private void buildIndex(MilvusServiceClient milvusClient) {
        milvusClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                        .withFieldName(PushMaterialConfig.Field.CASE_VECTOR)
                        .withIndexType(INDEX_TYPE)
                        .withMetricType(MetricType.L2)
                        .withExtraParam(INDEX_PARAM)
                        .withSyncMode(Boolean.FALSE)
                        .build()
        );
        System.out.println("Bilid Complete.");

    }

    /**載入Collection*/
    private void loadCollection(MilvusServiceClient milvusClient) {
        milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                        .build()
        );
        System.out.println("Load Collection Complete.");

    }

}
