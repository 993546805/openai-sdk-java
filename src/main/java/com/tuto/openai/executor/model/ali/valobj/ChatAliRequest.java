package com.tuto.openai.executor.model.ali.valobj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author tu
 * @date 2024-09-05 20:57
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAliRequest {
    private String model;

    private List<Message> messages;

    @JSONField(name = "stream_options")
    @Builder.Default
    private StreamOptions streamOptions = new StreamOptions();

    private boolean stream = false;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }



    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Getter
    @AllArgsConstructor
    public enum Role{
        USER("user"),
        SYSTEM("system"),
        ASSISTANT("assistant");
        private final String code;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class StreamOptions {
        @JSONField(name = "include_usage")
        private boolean includeUsage = true;
    }
}


