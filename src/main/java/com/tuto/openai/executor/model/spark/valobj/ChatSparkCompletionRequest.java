package com.tuto.openai.executor.model.spark.valobj;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author tu
 * @date 2024-09-04 21:54
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatSparkCompletionRequest {
    private String model;
    private boolean stream;
    @Builder.Default
    private double temperature = 1.0;
    private List<Tool> tools;
    private List<Message> messages;
    @JsonProperty("tool_choice")
    private String toolChoice;
    @JsonProperty("max_tokens")
    private int maxTokens;
    @JsonProperty("top_k")
    @Builder.Default
    private Integer topK = 4;

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
