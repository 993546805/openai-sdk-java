package com.tuto.openai.executor.model.chatglm;

import com.alibaba.fastjson.JSON;
import com.tuto.openai.executor.Executor;
import com.tuto.openai.executor.model.chatglm.config.ChatGLMConfig;
import com.tuto.openai.executor.model.chatglm.valobj.ChatGLMCompletionRequest;
import com.tuto.openai.executor.model.chatglm.valobj.ChatGLMCompletionResponse;
import com.tuto.openai.executor.model.chatglm.valobj.Model;
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

import java.util.*;


/**
 * @author tu
 * @date 2024-09-03 20:43
 */
@Slf4j
public class ChatGLMModelExecutor implements Executor, ParameterHandler<ChatGLMCompletionRequest>, ResultHandler {

    private final ChatGLMConfig chatGLMConfig;

    private final EventSource.Factory factory;


    public ChatGLMModelExecutor(Configuration configuration) {
        this.chatGLMConfig = configuration.getChatGLMConfig();
        this.factory = configuration.createRequestFactory();
    }

    @Override
    public EventSource completions(CompletionRequest completionRequest, EventSourceListener eventSourceListener) throws Exception {
        // 1.转换参数信息
        ChatGLMCompletionRequest chatGLMCompletionRequest = getParameterObject(completionRequest);
        // 2.构建请求信息
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + chatGLMConfig.getApiKey())
                .url(chatGLMConfig.getApiHost())
                .post(RequestBody.create(MediaType.parse(Configuration.APPLICATION_JSON), chatGLMCompletionRequest.toString()))
                .build();
        // 3.返回事件结果
        return factory.newEventSource(request, eventSourceListener(eventSourceListener));
    }

    @Override
    public ChatGLMCompletionRequest getParameterObject(CompletionRequest completionRequest) {
        Model model = Model.getModel(completionRequest.getModel());

        List<ChatGLMCompletionRequest.Message> prompts = new ArrayList<>();
        //重新组装参数,ChatGLM 需要用 Okay 间隔历史消息
        List<Message> messages = completionRequest.getMessages();
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (0 == i) {
                prompts.add(ChatGLMCompletionRequest.Message.builder()
                        .role(message.getRole())
                        .content(message.getContent())
                        .build());
            } else {
                String role = message.getRole();
                if (Objects.equals(role, CompletionRequest.Role.SYSTEM.getCode())) {
                    prompts.add(ChatGLMCompletionRequest.Message.builder()
                            .role(CompletionRequest.Role.SYSTEM.getCode())
                            .content(message.getContent())
                            .build());
                    prompts.add(ChatGLMCompletionRequest.Message.builder()
                            .role(CompletionRequest.Role.USER.getCode())
                            .content("Okay")
                            .build());
                } else {
                    prompts.add(ChatGLMCompletionRequest.Message.builder()
                            .role(CompletionRequest.Role.USER.getCode())
                            .content(message.getContent())
                            .build());

                    prompts.add(ChatGLMCompletionRequest.Message.builder()
                            .role(CompletionRequest.Role.USER.getCode())
                            .content("Okay")
                            .build());
                }
            }
        }

        return ChatGLMCompletionRequest.builder()
                .model(model)
                .temperature(completionRequest.getTemperature())
                .stream(completionRequest.isStream())
                .topP(completionRequest.getTopP())
                .messages(prompts)
                .build();
    }

    @Override
    public EventSourceListener eventSourceListener(EventSourceListener eventSourceListener) {
        return new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                if ("[DONE]".equals(data)) {
                    return;
                }
                ChatGLMCompletionResponse response = JSON.parseObject(data, ChatGLMCompletionResponse.class);
                List<ChatGLMCompletionResponse.Choice> choices = response.getChoices();
                Iterator<ChatGLMCompletionResponse.Choice> iterator = choices.iterator();

                while (iterator.hasNext()) {
                    ChatGLMCompletionResponse.Choice choice = iterator.next();
                    if ("stop".equals(choice.getFinish_reason())) {
                        CompletionResponse completionResponse = new CompletionResponse();
                        completionResponse.setId(response.getId());
                        completionResponse.setModel(response.getModel());
                        completionResponse.setChoices(Collections.singletonList(ChatChoice.builder().index(0).finishReason("stop").build()));
                        completionResponse.setCreated(response.getCreated());
                        Usage usage = new Usage();
                        usage.setTotalTokens(response.getUsage().getTotal_tokens());
                        usage.setPromptTokens(response.getUsage().getPrompt_tokens());
                        usage.setCompletionTokens(response.getUsage().getCompletion_tokens());
                        completionResponse.setUsage(usage);

                        eventSourceListener.onEvent(eventSource, id, type, JSON.toJSONString(completionResponse));
                        //处理结束
                        continue;
                    }


                    CompletionResponse completionResponse = new CompletionResponse();

                    ChatGLMCompletionResponse.Delta delta = choice.getDelta();
                    List<ChatChoice> returnChoices = new ArrayList<>();
                    ChatChoice chatChoice = new ChatChoice();
                    chatChoice.setDelta(Message.builder().name("").content(delta.getContent()).role(delta.getRole()).build());
                    returnChoices.add(chatChoice);

                    completionResponse.setId(response.getId());
                    completionResponse.setModel(response.getModel());
                    completionResponse.setChoices(returnChoices);
                    completionResponse.setCreated(response.getCreated());

                    eventSourceListener.onEvent(eventSource, id, type, JSON.toJSONString(completionResponse));
                }

//                if ("[DONE]".equals(data)) {
//                    eventSourceListener.onClosed(eventSource);
//                }else {
//                    ChatGLMCompletionResponse response = JSON.parseObject(data, ChatGLMCompletionResponse.class);
//
//                }
//                    CompletionResponse completionResponse = new CompletionResponse();
//                    List<ChatChoice> choices = new ArrayList<>();
//                    ChatChoice chatChoice = new ChatChoice();
//                    chatChoice.setDelta(new Message.Builder()
//                            .name("")
//                            .role(CompletionRequest.Role.SYSTEM)
//                            .content(response.getData())
//                            .build());
//                    choices.add(chatChoice);
//                    completionResponse.setChoices(choices);
//                    eventSourceListener.onEvent(eventSource, id, type, JSON.toJSONString(completionResponse));
//                } else if (EventType.finish.getCode().equals(type)) {
//                    ChatGLMCompletionResponse.Meta meta = JSON.parseObject(response.getMeta(), ChatGLMCompletionResponse.Meta.class);
//                    ChatGLMCompletionResponse.Usage chatGLMUsage = meta.getUsage();
//                    // 封装额度
//                    Usage usage = new Usage();
//                    usage.setPromptTokens(chatGLMUsage.getPrompt_tokens());
//                    usage.setCompletionTokens(chatGLMUsage.getCompletion_tokens());
//                    usage.setTotalTokens(chatGLMUsage.getTotal_tokens());
//
//                    //封装结束
//                    List<ChatChoice> choices = new ArrayList<>();
//                    ChatChoice chatChoice = new ChatChoice();
//                    chatChoice.setFinishReason("stop");
//                    chatChoice.setDelta(new Message.Builder()
//                            .name("")
//                            .role(CompletionRequest.Role.SYSTEM)
//                            .content(response.getData())
//                            .build());
//
//                    // 构建结果
//                    CompletionResponse completionResponse = new CompletionResponse();
//                    completionResponse.setChoices(choices);
//                    completionResponse.setUsage(usage);
//                    completionResponse.setCreated(System.currentTimeMillis());
//
//                    // 返回数据
//                    eventSourceListener.onEvent(eventSource, id, type, JSON.toJSONString(completionResponse));
//                } else {
//                    onClosed(eventSource);
//                }
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
