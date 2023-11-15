package com.thsrc.milvus;

import com.thsrc.milvus.services.MilvusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApplication implements CommandLineRunner {

    private final MilvusService milvusService;

    @Autowired
    public MyApplication(MilvusService milvusService) {
        this.milvusService = milvusService;
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
        milvusService.search();
    }
}