package com.tuto.openai.session;

import com.tuto.openai.executor.Executor;
import com.tuto.openai.executor.model.chatglm.config.ChatGLMConfig;
import com.tuto.openai.executor.model.chatglm.ChatGLMModelExecutor;
import com.tuto.openai.executor.model.chatgpt.config.ChatGPTConfig;
import com.tuto.openai.executor.model.spark.ChatSparkModelExecutor;
import com.tuto.openai.executor.model.spark.config.ChatSparkConfig;
import com.tuto.openai.executor.parameter.CompletionRequest;
import lombok.Data;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;

import java.util.HashMap;

/**
 * 配置文件
 * @author tu
 * @date 2024-09-03 20:34
 */
@Data
public class Configuration {
    /**
     * OpenAi 配置
     */
    private ChatGPTConfig chatGPTConfig;
    /**
     * ChatGLM 配置
     */
    private ChatGLMConfig chatGLMConfig;
    /**
     * ChatSpark 配置
     */
    private ChatSparkConfig chatSparkConfig;


    private OkHttpClient okHttpClient;


    private HashMap<String, Executor> executorGroup;

    public EventSource.Factory createRequestFactory() {
        return EventSources.createFactory(okHttpClient);
    }

    // OkHttp 配置信息
    private HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.HEADERS;
    private long connectTimeout = 4500;
    private long writeTimeout = 4500;
    private long readTimeout = 4500;

    // http keywords
    public static final String SSE_CONTENT_TYPE = "text/event-stream";
    public static final String DEFAULT_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)";
    public static final String APPLICATION_JSON = "application/json";
    public static final String JSON_CONTENT_TYPE = APPLICATION_JSON + "; charset=utf-8";

    public HashMap<String, Executor> newExecutorGroup(){
        this.executorGroup = new HashMap<>();
        // ChatGLM 类型执行器填充
        Executor chatGLMExecutor = new ChatGLMModelExecutor(this);
        Executor sparkExecutor = new ChatSparkModelExecutor(this);
        executorGroup.put(CompletionRequest.Model.CHAT_GLM_4_FLASH.getCode(), chatGLMExecutor);
        executorGroup.put(CompletionRequest.Model.SPARK_GENERAL.getCode(), sparkExecutor);
        executorGroup.put(CompletionRequest.Model.ULTRA_4.getCode(), sparkExecutor);

        return this.executorGroup;
    }
}
