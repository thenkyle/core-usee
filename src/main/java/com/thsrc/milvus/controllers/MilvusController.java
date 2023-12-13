package com.thsrc.milvus.controllers;

import com.thsrc.milvus.services.MilvusService;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kylelin
 */
@RestController
public class MilvusController {
    @Autowired
    private final MilvusServiceClient milvusServiceClient;
    @Autowired
    private MilvusService milvusService;

    public MilvusController(MilvusServiceClient milvusServiceClient) {
        this.milvusServiceClient = milvusServiceClient;
    }

    @GetMapping("/milvus-connect")
    public String milvusConnectTest(){
        return this.milvusServiceClient.getVersion().toString();
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

