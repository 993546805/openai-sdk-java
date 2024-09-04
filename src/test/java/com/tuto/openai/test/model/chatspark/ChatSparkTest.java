package com.tuto.openai.test.model.chatspark;

import com.alibaba.fastjson.JSON;
import com.tuto.openai.executor.model.spark.config.ChatSparkConfig;
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
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * @author tu
 * @date 2024-09-04 22:29
 */
@Slf4j
public class ChatSparkTest {

    private OpenAiSession openAiSession;

    @Before
    public void test_openAiSession() {
        Properties prop = new Properties();
        ClassLoader classLoader = ChatSparkTest.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(".env")) {
            prop.load(inputStream);
            ChatSparkConfig chatSparkConfig = new ChatSparkConfig();
            chatSparkConfig.setApiHost(prop.getProperty("spark.config.apiHost"));
            chatSparkConfig.setApiKey(prop.getProperty("spark.config.apiKey"));

            // 2.配置文件
            Configuration configuration = new Configuration();
            configuration.setChatSparkConfig(chatSparkConfig);
            configuration.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            // 3.创建会话工厂
            OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);

            // 4. 开启会话
            openAiSession = factory.openSession();
        } catch (Exception ignore) {

        }
    }

    @Test
    public void test_completions() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);

        CompletionRequest completionRequest = new CompletionRequest();
        completionRequest.setModel(CompletionRequest.Model.SPARK_GENERAL.getCode());
        completionRequest.setStream(true);
        completionRequest.setMessages(Collections.singletonList(Message.builder()
                .role(CompletionRequest.Role.USER.getCode())
                .content("帮我写一个 java 冒泡排序")
                .build()));
        openAiSession.completions(completionRequest, new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                log.info("onOpen");
            }

            @Override
            public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
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

            @Override
            public void onClosed(EventSource eventSource) {
                log.info("onClosed");
                latch.countDown();
            }

            @Override
            public void onFailure(EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                log.error("onFailure", t);
                latch.countDown();
            }
        });

        latch.await();
    }
}
