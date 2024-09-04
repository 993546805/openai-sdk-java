package com.tuto.openai.executor.model.chatglm.valobj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tu
 * @date 2024-09-03 21:48
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGLMCompletionRequest {

    /**
     * 模型
     */
    private Model model = Model.CHATGLM_6B_SSE;

    private boolean stream = false;
    /**
     * 请求ID
     */
    @JsonProperty("request_id")
    @Builder.Default
    private String requestId = String.format("xfg-%d", System.currentTimeMillis());
    /**
     * 控制温度【随机性】
     */
    private double temperature = 0.9d;
    /**
     * 多样性控制；
     */
    @JsonProperty("top_p")
    private double topP = 0.7d;
    /**
     * 输入给模型的会话信息
     * 用户输入的内容；role=user
     * 挟带历史的内容；role=assistant
     */
    private List<Message> messages;
    /**
     * sseformat, 用于兼容解决sse增量模式okhttpsse截取data:后面空格问题, [data: hello]。只在增量模式下使用sseFormat。
     */

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Override
    public String toString() {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("request_id", requestId);
        paramsMap.put("messages", messages);
        paramsMap.put("temperature", temperature);
        paramsMap.put("top_p", topP);
        paramsMap.put("model", model.getCode());
        paramsMap.put("stream", stream);
        try {
            return new ObjectMapper().writeValueAsString(paramsMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
