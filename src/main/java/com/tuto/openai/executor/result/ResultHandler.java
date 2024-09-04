package com.tuto.openai.executor.result;

import okhttp3.sse.EventSourceListener;

/**
 * @author tu
 * @date 2024-09-03 21:56
 */
public interface ResultHandler {

    EventSourceListener eventSourceListener(EventSourceListener eventSourceListener);
}
