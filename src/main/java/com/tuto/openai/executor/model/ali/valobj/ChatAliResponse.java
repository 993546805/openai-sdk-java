package com.tuto.openai.executor.model.ali.valobj;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author tu
 * @date 2024-09-05 21:39
 */
@Data
public class ChatAliResponse {
    private String id;
    private String object;
    private Integer created;
    private String model;
    @JSONField(name = "usage")
    private Usage usage;
    private List<Choice> choices;
    @JSONField(name = "system_fingerprint")
    private String systemFingerprint;

    @Data
    public static class Usage {
        @JSONField(name = "prompt_tokens")
        private int promptTokens;
        @JSONField(name = "completion_tokens")
        private int completionTokens;
        @JSONField(name = "total_tokens")
        private int totalTokens;
    }

    @Data
    public static class Choice {
        @JSONField(name = "finish_reason")
        private String finishReason;
        private Message delta;
        private Integer index;
        @JSONField(name = "logprob")
        private String logprob;
    }


    @Data
    public static class Message {
        private String role;
        private String content;
    }


}
