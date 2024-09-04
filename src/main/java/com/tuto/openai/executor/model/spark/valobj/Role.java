package com.tuto.openai.executor.model.spark.valobj;

/**
 * @author tu
 * @date 2024-09-04 22:23
 */
public enum Role {
    ASSISTANT("assistant"),
    USER("user"),
    SYSTEM("system");

    private final String code;

    Role(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
