package com.tuto.openai.session.defaults;

import com.tuto.openai.executor.Executor;
import com.tuto.openai.executor.interceptor.HTTPInterceptor;
import com.tuto.openai.session.Configuration;
import com.tuto.openai.session.OpenAiSession;
import com.tuto.openai.session.OpenAiSessionFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 会话工厂实现
 * @author tu
 * @date 2024-09-03 21:42
 */
public class DefaultOpenAiSessionFactory implements OpenAiSessionFactory{

    private final Configuration configuration;

    public DefaultOpenAiSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public OpenAiSession openSession() {
        // 1.日志配置
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(configuration.getLevel());
        // 2.开启 http 客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new HTTPInterceptor(configuration))
                .connectTimeout(configuration.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(configuration.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(configuration.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .build();
        configuration.setOkHttpClient(okHttpClient);
        // 3.创建执行器组
        HashMap<String, Executor> executorGroup = configuration.newExecutorGroup();
        // 4.创建会话服务
        return new DefaultOpenAiSession(configuration, executorGroup);
    }
}
