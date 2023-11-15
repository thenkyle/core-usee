package com.thsrc.milvus.services;

import com.thsrc.milvus.utils.PushMaterialConfig;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.BoolResponse;
import io.milvus.grpc.DataType;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.*;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


@Service
public class MilvusService {
    private final MilvusClient milvusClient;

    Logger logger = LogManager.getLogger(getClass());

    static final IndexType INDEX_TYPE = IndexType.IVF_FLAT;
    static final String INDEX_PARAM = "{\"nlist\":1024}";

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
        final Integer SEARCH_K = 2;
        final String SEARCH_PARAM = "{\"nprobe\":10}";
        List<String> search_output_fields = Arrays.asList(PushMaterialConfig.Field.BOOK_ID);
        List<List<Float>> search_vectors = Arrays.asList(Arrays.asList(0.1f, 0.2f));

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                .withMetricType(MetricType.L2)
                .withOutFields(search_output_fields)
                .withTopK(SEARCH_K)
                .withVectors(search_vectors)
                .withVectorFieldName(PushMaterialConfig.Field.BOOK_INTRO)
                .withParams(SEARCH_PARAM)
                .build();

        R<SearchResults> respSearch = milvusClient.search(searchParam);
        SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(respSearch.getData().getResults());
        System.out.println("wrapperSearch.getIDScore" + wrapperSearch.getIDScore(0));
        System.out.println("wrapperSearch.getFieldData" + wrapperSearch.getFieldData(PushMaterialConfig.Field.BOOK_ID, 0));
        // 釋放Collection
//        milvusClient.releaseCollection(
//                ReleaseCollectionParam.newBuilder()
//                        .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
//                        .build());
    }

    /**建立Collection所需參數*/
    private CreateCollectionParam createCollectionReq() {
        FieldType fieldType1 = FieldType.newBuilder()
                .withName(PushMaterialConfig.Field.BOOK_ID)
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();
        FieldType fieldType2 = FieldType.newBuilder()
                .withName(PushMaterialConfig.Field.WORK_COUNT)
                .withDataType(DataType.Int64)
                .build();
        FieldType fieldType3 = FieldType.newBuilder()
                .withName(PushMaterialConfig.Field.BOOK_INTRO)
                .withDataType(DataType.FloatVector)
                .withDimension(2)
                .build();

        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                .withDescription(PushMaterialConfig.DESCRIPTION)
                .withShardsNum(2)
                .addFieldType(fieldType1)
                .addFieldType(fieldType2)
                .addFieldType(fieldType3)
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
        List<Long> book_id_array = new ArrayList<>();
        List<Long> word_count_array = new ArrayList<>();
        List<List<Float>> book_intro_array = new ArrayList<>();
        for (long i = 0L; i < 2000; ++i) {
            book_id_array.add(i);
            word_count_array.add(i + 10000);
            List<Float> vector = new ArrayList<>();
            for (int k = 0; k < 2; ++k) {
                vector.add(ran.nextFloat());
            }
            book_intro_array.add(vector);
        }

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(PushMaterialConfig.Field.BOOK_ID,  book_id_array));
        fields.add(new InsertParam.Field(PushMaterialConfig.Field.WORK_COUNT, word_count_array));
        fields.add(new InsertParam.Field(PushMaterialConfig.Field.BOOK_INTRO, book_intro_array));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                .withPartitionName(PushMaterialConfig.PARTITION_NAME)
                .withFields(fields)
                .build();
        logger.info(insertParam);
        milvusClient.insert(insertParam);
        System.out.println("Insert Data Complete.");

    }

    private void buildIndex(MilvusServiceClient milvusClient) {
        milvusClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName(PushMaterialConfig.COLLECTION_NAME)
                        .withFieldName(PushMaterialConfig.Field.BOOK_INTRO)
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
