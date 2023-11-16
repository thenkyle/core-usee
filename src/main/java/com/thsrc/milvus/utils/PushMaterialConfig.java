package com.thsrc.milvus.utils;

import org.springframework.stereotype.Component;

/**
 * @author kylelin
 */
@Component
public class PushMaterialConfig {
    /**
     * Collection名稱
     */
    public static final String COLLECTION_NAME = "case";
    /**
    *  Partition名稱
    * */
    public static final String PARTITION_NAME = "novel";
    /**
     * 分片数量
     */
//    public static final Integer SHARDS_NUM = 8;
    /**
     * Partition數量
     */
//    public static final Integer PARTITION_NUM = 16;

    /**
     * Partition前缀
     */
//    public static final String PARTITION_PREFIX = "shards_";
    /**
     * 特征值長度
     */
    public static final Integer FEATURE_DIM = 256;
    public static final String DESCRIPTION = "Test search";

    /**
     * 欄位
     */
    public static class Field {
        /**
         * PK
         */
        public static final String CASE_ID = "case_id";
        /**
         * 字數欄位
         */
        public static final String ACTION = "action";
        public static final String DEPARTRUE_ARRIVE = "departure_Arrive";
        public static final String PROFILE = "profile";

        /**
         * Vector
         */
        public static final String CASE_VECTOR = "case_vector";
    }


}
