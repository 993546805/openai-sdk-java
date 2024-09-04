package com.tuto.openai.test.model.chatglm;

import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSON;
import com.tuto.openai.executor.model.chatglm.config.ChatGLMConfig;
import com.tuto.openai.executor.parameter.ChatChoice;
import com.tuto.openai.executor.parameter.CompletionRequest;
import com.tuto.openai.executor.parameter.CompletionResponse;
import com.tuto.openai.executor.parameter.Message;
import com.tuto.openai.session.Configuration;
import com.tuto.openai.session.OpenAiSession;
import com.tuto.openai.session.OpenAiSessionFactory;
import com.tuto.openai.session.defaults.DefaultOpenAiSessionFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * 智谱AI模型测试
 * @author tu
 * @date 2024-09-03 22:30
 */
@Slf4j
public class ChatGLMTest {

    private OpenAiSession openAiSession;


    @Before
    public void test_OpenAiSessionFactory() throws IOException {
        Properties properties = new Properties();
        ClassLoader classLoader = ChatGLMTest.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream(".env");
        properties.load(resourceAsStream);
        ChatGLMConfig chatGLMConfig = new ChatGLMConfig();
        chatGLMConfig.setApiHost(properties.getProperty("chatglm.config.apiHost"));
        chatGLMConfig.setApiKey(properties.getProperty("chatglm.config.apiKey"));

        // 2. 配置文件
        Configuration configuration = new Configuration();
        configuration.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        configuration.setChatGLMConfig(chatGLMConfig);

        // 3. 创建会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);

        // 4. 开启会话
        this.openAiSession = factory.openSession();
    }

    /**
     * 用于测试:文本 流式对话
     */
    @Test
    public void test_completions() throws Exception {
        CompletionRequest request = CompletionRequest.builder()
                .stream(true)
                .model(CompletionRequest.Model.CHAT_GLM_4_FLASH.getCode())
                .messages(Collections.singletonList(new Message.Builder().role(CompletionRequest.Role.USER).content("1+1").build()))
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        openAiSession.completions(request, new EventSourceListener() {

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                log.info("OpenAI 会话关闭");
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                log.error("OpenAI 会话异常", t);
                countDownLatch.countDown();
            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                if ("[DONE]".equalsIgnoreCase(data)) {
                    log.info("OpenAI 答应完成");
                    return;
                }

                CompletionResponse response = JSON.parseObject(data, CompletionResponse.class);
                List<ChatChoice> choices = response.getChoices();
                for (ChatChoice choice : choices) {
                    Message delta = choice.getDelta();
                    // 答应完成
                    String finishReason = choice.getFinishReason();
                    if(StringUtils.isNoneBlank(finishReason) && "stop".equalsIgnoreCase(finishReason)){
                        log.info("会话结束,用量信息:{}",JSON.toJSONString(response.getUsage()));
                        return;
                    }

                    log.info("测试结果: {}", delta.getContent());
                }
            }
        });

        countDownLatch.await();
    }
}
