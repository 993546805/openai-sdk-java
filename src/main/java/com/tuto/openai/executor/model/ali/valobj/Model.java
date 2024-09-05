package com.tuto.openai.executor.model.ali.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author tu
 * @date 2024-09-05 21:02
 */
@Getter
@AllArgsConstructor
public enum Model {

    QWEN_MAX("qwen-max"),
    QWEN_VL_MAX_0809("qwen-vl-max-0809"),
    //qwen-turbo
    QWEN_TURBO("qwen-turbo"),
    ;
    private final String code;


    public static Model getModel(String code) {
        for (Model model : Model.values()) {
            if (model.code.equals(code)) {
                return model;
            }
        }
        return null;
    }
}
