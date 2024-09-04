package com.tuto.openai.executor.model.spark.valobj;

/**
 * @author tu
 * @date 2024-09-04 22:24
 */
public enum Model {
    GENERAL("general"),
    GENERAL_V3("generalv3"),
    GENERAL_V35("generalv3.5"),
    GENERAL_4_0("4.0Ultra"),
    PRO_128K("pro-128k");

    private final String code;

    Model(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }


    public static Model getModel(String code) {
        for (Model model : values()) {
            if (model.code.equals(code)) {
                return model;
            }
        }
        return GENERAL;
    }

}
