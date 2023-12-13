package com.thsrc.milvus.services;

import com.github.jfasttext.JFastText;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VectorizationService {

    // 相對於 src/main/resources 目錄的路徑
    private static final String MODEL_PATH = "src/main/resources/models/model_text.bin";

    public float[] getFieldVector(String fieldValue) {
        // 載入FastText模型
        JFastText jft = new JFastText();
        jft.loadModel(MODEL_PATH);

        // 將非數字型欄位的值轉換為特徵碼
        List<Float> floatList = jft.getVector(fieldValue);
        System.out.println("floatList -> "+floatList);

        // 將List<Float> 轉換為 float[]
        float[] fieldVector = new float[floatList.size()];
        for (int i = 0; i < floatList.size(); i++) {
            fieldVector[i] = floatList.get(i);
        }

        return fieldVector;
    }

    private boolean isTextField(String field) {
        // 根據你的邏輯判斷欄位是否為文本欄位
        // 這僅僅是一個示例，你可能需要根據實際情況修改這個方法
        return field.matches(".*[a-zA-Z]+.*");
    }


}
