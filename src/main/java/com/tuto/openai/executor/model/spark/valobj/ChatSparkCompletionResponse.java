package com.tuto.openai.executor.model.spark.valobj;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author tu
 * @date 2024-09-04 22:54
 */
@Data
public class ChatSparkCompletionResponse {
    private Integer code;
    private String message;
    @JsonProperty("sid")
    private String sId;
    private String id;
    private Integer created;
    private List<Choice> choices;
    private Usage usage;

    @Data
    public static class Choice {
        private Delta delta;
        private Integer index;
    }

    @Data
    public static class Delta {
        private String role;
        private String content;
    }

    @Data
    public static class Usage {
        @JSONField(name = "prompt_tokens")
        private Integer promptTokens;
        @JSONField(name = "completion_tokens")
        private Integer completionTokens;
        @JSONField(name = "total_tokens")
        private Integer totalTokens;
    }
}
