package com.tuto.openai.executor.model.chatglm.valobj;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 应答参数
 * @author tu
 */
@Data
public class ChatGLMCompletionResponse {

    private String id;
    private Long created;
    private String model;
    private List<Choice> choices;
    private String meta;
    private Usage usage;

    @Data
    public static class Choice {
        @JsonProperty("finish_reason")
        private String finish_reason;
        private int index;
        private Delta delta;
    }

    @Data
    public static class Delta {
        private String role;
        private String content;
    }



    @Data
    public static class Meta {
        private String task_status;
        private Usage usage;
        private String task_id;
        private String request_id;
    }

    @Data
    public static class Usage {
        private int completion_tokens;
        private int prompt_tokens;
        private int total_tokens;
    }

}
