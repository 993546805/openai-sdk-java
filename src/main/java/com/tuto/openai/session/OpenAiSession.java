package com.tuto.openai.session;

import com.tuto.openai.executor.parameter.CompletionRequest;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

/**
 * OpenAi 会话接口
 * @author tu
 * @date 2024-09-03 20:22
 */
public interface OpenAiSession  {
    /**
     * 问答模型,流式反馈
     * @param completionRequest 请求信息
     * @param eventSourceListener 实时监听;通过监听 onEvent 方法接收数据
     * @return 答应结果
     * @throws Exception 异常
     */
    EventSource completions(CompletionRequest completionRequest, EventSourceListener eventSourceListener) throws Exception;
}
