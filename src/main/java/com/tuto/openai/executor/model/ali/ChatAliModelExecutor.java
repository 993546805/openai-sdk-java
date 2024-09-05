package com.tuto.openai.executor.model.ali;

import com.alibaba.fastjson.JSON;
import com.tuto.openai.executor.Executor;
import com.tuto.openai.executor.model.ali.config.ChatAliConfig;
import com.tuto.openai.executor.model.ali.valobj.ChatAliRequest;
import com.tuto.openai.executor.model.ali.valobj.ChatAliResponse;
import com.tuto.openai.executor.model.ali.valobj.Model;
import com.tuto.openai.executor.parameter.*;
import com.tuto.openai.executor.result.ResultHandler;
import com.tuto.openai.session.Configuration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tu
 * @date 2024-09-05 20:55
 */
@Slf4j
public class ChatAliModelExecutor implements Executor, ParameterHandler<ChatAliRequest>, ResultHandler {

    private final ChatAliConfig aliConfig;

    private final EventSource.Factory factory;


    public ChatAliModelExecutor(Configuration configuration) {
        this.aliConfig = configuration.getAliConfig();
        this.factory = configuration.createRequestFactory();
    }

    @Override
    public EventSource completions(CompletionRequest completionRequest, EventSourceListener eventSourceListener) throws Exception {
        // 1.转换参数信息
        ChatAliRequest chatAliRequest = getParameterObject(completionRequest);

        // 2.构建请求
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + aliConfig.getApiKey())
                .url(aliConfig.getApiHost())
                .post(RequestBody.create(MediaType.parse(Configuration.APPLICATION_JSON), chatAliRequest.toString()))
                .build();

        // 3.返回事件结果
        return factory.newEventSource(request, eventSourceListener(eventSourceListener));
    }

    @Override
    public ChatAliRequest getParameterObject(CompletionRequest completionRequest) {
        String model = Model.getModel(completionRequest.getModel()).getCode();

        List<ChatAliRequest.Message> messages = new ArrayList<>();
        for (Message message : completionRequest.getMessages()) {
            if (CompletionRequest.Role.ASSISTANT.getCode().equals(message.getRole())) {
                messages.add(ChatAliRequest.Message.builder()
                        .role(ChatAliRequest.Role.ASSISTANT.getCode())
                        .content(message.getContent())
                        .build());
            } else if (CompletionRequest.Role.USER.getCode().equals(message.getRole())) {
                messages.add(ChatAliRequest.Message.builder()
                        .role(ChatAliRequest.Role.USER.getCode())
                        .content(message.getContent())
                        .build());
            } else if (CompletionRequest.Role.SYSTEM.getCode().equals(message.getRole())) {
                messages.add(ChatAliRequest.Message.builder()
                        .role(ChatAliRequest.Role.SYSTEM.getCode())
                        .content(message.getContent())
                        .build());
            }
        }


        return ChatAliRequest.builder()
                .model(model)
                .messages(messages)
                .stream(completionRequest.isStream())
                .build();
    }

    @Override
    public EventSourceListener eventSourceListener(EventSourceListener eventSourceListener) {
        return new EventSourceListener() {
            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                eventSourceListener.onOpen(eventSource, response);
            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                if ("[DONE]".equals(data)) {
                    return;
                }

                // 1.处理信息
                ChatAliResponse chatAliResponse = JSON.parseObject(data, ChatAliResponse.class);
                Usage usage = null;
                List<ChatChoice> choices = new ArrayList<>();

                // 1.1处理使用量信息
                if (null != chatAliResponse.getUsage()) {
                    usage = new Usage();
                    usage.setPromptTokens(chatAliResponse.getUsage().getPromptTokens());
                    usage.setCompletionTokens(chatAliResponse.getUsage().getCompletionTokens());
                    usage.setTotalTokens(chatAliResponse.getUsage().getTotalTokens());

                    // 阿里独立返回用量信息特殊处理
                    choices.add(ChatChoice.builder()
                            .index(0)
                            .finishReason("stop")
                            .delta(Message.builder().role(ChatAliRequest.Role.ASSISTANT.getCode()).content("").build())
                            .build());

                }
                // 1.2 处理消息
                for (ChatAliResponse.Choice choice : chatAliResponse.getChoices()) {

                    String content = choice.getDelta().getContent();

                    choices.add(ChatChoice.builder()
                            .index(choice.getIndex())
                            .finishReason(usage != null ? "stop" : null)
                            .delta(Message.builder().role(ChatAliRequest.Role.ASSISTANT.getCode()).content(content).build())
                            .build());
                }

                // 2.封装信息
                CompletionResponse completionResponse = new CompletionResponse();
                completionResponse.setId(chatAliResponse.getId());
                completionResponse.setObject(chatAliResponse.getObject());
                completionResponse.setModel(chatAliResponse.getModel());
                completionResponse.setChoices(choices);
                completionResponse.setCreated(chatAliResponse.getCreated());
                completionResponse.setUsage(usage);
                completionResponse.setSystemFingerprint(chatAliResponse.getSystemFingerprint());

                // 3.返回结果
                eventSourceListener.onEvent(eventSource, id, type, JSON.toJSONString(completionResponse));
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                eventSourceListener.onClosed(eventSource);
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                eventSourceListener.onFailure(eventSource, t, response);
            }
        };
    }
}
