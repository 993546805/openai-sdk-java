package com.tuto.openai.executor.interceptor;

import com.tuto.openai.session.Configuration;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author tu
 * @date 2024-09-03 21:57
 */
public class HTTPInterceptor implements Interceptor {


    private final Configuration configuration;

    public HTTPInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        // 1. 获取原始 Request
        Request original = chain.request();

        // 2. 构建请求
        Request request = original.newBuilder()
                .url(original.url())
                .header("Content-Type", Configuration.JSON_CONTENT_TYPE)
                .header("User-Agent", Configuration.DEFAULT_USER_AGENT)
                .header("Accept", Configuration.SSE_CONTENT_TYPE)
                .method(original.method(), original.body())
                .build();

        // 3. 返回执行结果
        return chain.proceed(request);
    }
}
