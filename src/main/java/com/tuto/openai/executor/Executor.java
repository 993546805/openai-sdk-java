package com.tuto.openai.executor;

import com.tuto.openai.executor.parameter.CompletionRequest;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

/**
 * @author tu
 */
public interface Executor {

    /**
     * 问答模式，流式反馈
     *
     * @param completionRequest   请求信息
     * @param eventSourceListener 实现监听；通过监听的 onEvent 方法接收数据
     * @return 应答结果
     * @throws Exception 异常
     */
    EventSource completions(CompletionRequest completionRequest, EventSourceListener eventSourceListener) throws Exception;
}
