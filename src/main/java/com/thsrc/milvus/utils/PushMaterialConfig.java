package com.thsrc.milvus.utils;

/**
 * @author kylelin
 */
public class PushMaterialConfig {
    /**
     * Collection名稱
     */
    public static final String COLLECTION_NAME = "book";
    /**
    *  Partition名稱
    * */
    public static final String PARTITION_NAME = "novel";
    /**
     * 分片数量
     */
//    public static final Integer SHARDS_NUM = 8;
    /**
     * 分区数量
     */
//    public static final Integer PARTITION_NUM = 16;

    /**
     * 分区前缀
     */
//    public static final String PARTITION_PREFIX = "shards_";
    /**
     * 特征值长度
     */
    public static final Integer FEATURE_DIM = 256;
    public static final String DESCRIPTION = "Test book search";

    /**
     * 欄位
     */
    public static class Field {
        /**
         * PK
         */
        public static final String BOOK_ID = "book_id";
        /**
         * 字數欄位
         */
        public static final String WORK_COUNT = "word_count";
        /**
         * Vector
         */
        public static final String BOOK_INTRO = "book_intro";
    }


}
