package com.thsrc.milvus.controllers;

import com.thsrc.milvus.services.MilvusService;
import io.milvus.client.MilvusServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kylelin
 */
@RestController
@RequestMapping("/api")
public class MilvusController {
    @Autowired
    private final MilvusServiceClient milvusServiceClient;
    @Autowired
    private MilvusService milvusService;

    public MilvusController(MilvusServiceClient milvusServiceClient) {
        this.milvusServiceClient = milvusServiceClient;
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

    @GetMapping("/connect")
    public String milvusConnectTest(){
        return this.milvusServiceClient.getVersion().toString();
    }

    @GetMapping("/search")
    public List<Long> search() {
        List<Long> searchResultsWrapper = milvusService.search();
        return searchResultsWrapper;
    }

}

