package com.thsrc.milvus;

import com.thsrc.milvus.services.MilvusService;
import com.thsrc.milvus.services.VectorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApplication implements CommandLineRunner {

    private final MilvusService milvusService;
    private final VectorizationService vectorizationService;

    @Autowired
    public MyApplication(MilvusService milvusService, VectorizationService vectorizationService) {
        this.milvusService = milvusService;
        this.vectorizationService = vectorizationService;

    }

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        milvusService.initializeMilvus();
        milvusService.insertData();
        milvusService.buildIndex();
        milvusService.loadCollection();
//        milvusService.search();
    }
}