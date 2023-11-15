package com.thsrc.milvus.controllers;

import com.thsrc.milvus.services.MilvusService;
import io.milvus.client.MilvusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kylelin
 */
@RestController
public class MilvusController {
    @Autowired
    private final MilvusClient milvusServiceClient;
    @Autowired
    private MilvusService milvusService;

    public MilvusController(MilvusClient milvusClient) {
        this.milvusServiceClient = milvusClient;
    }

    public void initializeMilvus() {
        milvusService.initializeMilvus();
    }

    public void insertData() {
        milvusService.insertData();
    }

    public void buildIndex() {
        milvusService.buildIndex();
    }

    public void loadCollection() { milvusService.loadCollection(); }

    public void search() {
        milvusService.search();
    }

}

