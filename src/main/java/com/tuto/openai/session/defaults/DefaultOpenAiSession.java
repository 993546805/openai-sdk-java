package com.tuto.openai.session.defaults;

import com.tuto.openai.executor.Executor;
import com.tuto.openai.executor.parameter.CompletionRequest;
import com.tuto.openai.session.Configuration;
import com.tuto.openai.session.OpenAiSession;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import java.util.HashMap;

/**
 * @author tu
 * @date 2024-09-03 21:41
 */
public class DefaultOpenAiSession implements OpenAiSession {
    private final Configuration configuration;
    private final HashMap<String, Executor> executorGroup;

    public DefaultOpenAiSession(Configuration configuration, HashMap<String, Executor> executorGroup) {
        this.configuration = configuration;
        this.executorGroup = executorGroup;
    }

    @Override
    public EventSource completions(CompletionRequest completionRequest, EventSourceListener eventSourceListener) throws Exception {
        // 1.选择执行器
        Executor executor = executorGroup.get(completionRequest.getModel());
        if (null == executor) {
            throw new RuntimeException(completionRequest.getModel() + "模型执行器尚未实现!");
        }
        return executor.completions(completionRequest, eventSourceListener);
    }
}
