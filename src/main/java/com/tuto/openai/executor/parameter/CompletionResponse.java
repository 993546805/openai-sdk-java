package com.tuto.openai.executor.parameter;

import lombok.Data;

import java.util.List;

/**
 * 对话请求结果
 * @author tu
 * @date 2024-09-03 22:15
 */
@Data
public class CompletionResponse {

    private String id;
    private String object;
    private String model;
    private List<ChatChoice> choices;
    private long created;
    private Usage usage;
    private String systemFingerprint;
}
