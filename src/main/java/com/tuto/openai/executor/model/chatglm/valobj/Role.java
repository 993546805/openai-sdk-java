package com.tuto.openai.executor.model.chatglm.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author tu
 * @date 2024-09-03 21:52
 */
@Getter
@AllArgsConstructor
public enum Role {

    USER("user"),
    ASSISTANT("assistant");
    private final String code;

}
