package com.tuto.openai.executor.model.spark;

import com.alibaba.fastjson.JSON;
import com.tuto.openai.executor.Executor;
import com.tuto.openai.executor.model.spark.config.ChatSparkConfig;
import com.tuto.openai.executor.model.spark.valobj.*;
import com.tuto.openai.executor.model.spark.valobj.Message;
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
import java.util.Objects;

/**
 * 讯飞星火模型执行器
 *
 * @author tu
 * @date 2024-09-04 21:55
 */
@Slf4j
public class ChatSparkModelExecutor implements Executor, ParameterHandler<ChatSparkCompletionRequest>, ResultHandler {

    private final ChatSparkConfig chatSparkConfig;

    private final EventSource.Factory factory;

    public ChatSparkModelExecutor(Configuration configuration) {
        this.chatSparkConfig = configuration.getChatSparkConfig();
        this.factory = configuration.createRequestFactory();
    }

    @Override
    public EventSource completions(CompletionRequest completionRequest, EventSourceListener eventSourceListener) throws Exception {
        // 1.转换参数信息
        ChatSparkCompletionRequest chatSparkCompletionRequest = getParameterObject(completionRequest);
        // 2.构建请求信息
        Request request = new Request.Builder()
                .url(chatSparkConfig.getApiHost())
                .header("Authorization", "Bearer " + chatSparkConfig.getApiKey())
                .post(RequestBody.create(MediaType.parse(Configuration.APPLICATION_JSON), chatSparkCompletionRequest.toString()))
                .build();
        // 3.返回事件结果
        return factory.newEventSource(request, eventSourceListener(eventSourceListener));
    }

    @Override
    public ChatSparkCompletionRequest getParameterObject(CompletionRequest completionRequest) {
        ChatSparkCompletionRequest chatSparkCompletionRequest = new ChatSparkCompletionRequest();

        Model model = Model.getModel(completionRequest.getModel());

        ArrayList<Message> messages = new ArrayList<>();
        for (com.tuto.openai.executor.parameter.Message message : completionRequest.getMessages()) {
            if (Objects.equals(message.getRole(), CompletionRequest.Role.USER.getCode())) {
                messages.add(Message.builder()
                        .role(Role.USER.getCode())
                        .content(message.getContent())
                        .build());
            } else if (Objects.equals(message.getRole(), CompletionRequest.Role.ASSISTANT.getCode())) {
                messages.add(Message.builder()
                        .role(Role.ASSISTANT.getCode())
                        .content(message.getContent())
                        .build());
            } else {
                messages.add(Message.builder()
                        .role(Role.SYSTEM.getCode())
                        .content(message.getContent())
                        .build());
            }
        }

        chatSparkCompletionRequest.setModel(model.getCode());
        chatSparkCompletionRequest.setStream(completionRequest.isStream());
        chatSparkCompletionRequest.setTemperature(completionRequest.getTemperature());
        chatSparkCompletionRequest.setMessages(messages);
        chatSparkCompletionRequest.setMaxTokens(completionRequest.getMaxTokens());
        return chatSparkCompletionRequest;
    }

    @Override
    public EventSourceListener eventSourceListener(EventSourceListener eventSourceListener) {
        return new EventSourceListener() {

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                if ("[DONE]".equals(data)) {
                    return;
                }
                ChatSparkCompletionResponse chatSparkCompletionResponse = JSON.parseObject(data, ChatSparkCompletionResponse.class);

                Usage resUsage = null;
                ChatSparkCompletionResponse.Usage usage = chatSparkCompletionResponse.getUsage();
                if (null != usage){
                    resUsage = new Usage();
                    resUsage.setCompletionTokens(usage.getCompletionTokens());
                    resUsage.setPromptTokens(usage.getPromptTokens());
                    resUsage.setTotalTokens(usage.getTotalTokens());
                }

                ArrayList<ChatChoice> choices = new ArrayList<>();

                for (ChatSparkCompletionResponse.Choice choice : chatSparkCompletionResponse.getChoices()) {
                    if(!Role.ASSISTANT.getCode().equals(choice.getDelta().getRole())){
                       continue;
                    }
                    ChatChoice chatChoice = ChatChoice.builder()
                            .index(choice.getIndex())
                            .delta(com.tuto.openai.executor.parameter.Message.builder().content(choice.getDelta().getContent()).role(choice.getDelta().getRole()).build())
                            .finishReason(resUsage == null ? null: "stop")
                            .build();
                    choices.add(chatChoice);
                }



                CompletionResponse completionResponse = new CompletionResponse();
                completionResponse.setId(chatSparkCompletionResponse.getId());
                completionResponse.setUsage(resUsage);
                completionResponse.setChoices(choices);
                completionResponse.setCreated(chatSparkCompletionResponse.getCreated());
                eventSourceListener.onEvent(eventSource, id, type, JSON.toJSONString(completionResponse));

            }
            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                eventSourceListener.onOpen(eventSource, response);
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
