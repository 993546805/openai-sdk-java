package com.tuto.openai.executor.parameter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 对话请求对象
 * @author tu
 * @date 2024-09-03 20:25
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionRequest implements Serializable {

    /**
     * 默认模型
     */
    private String model = Model.GPT_3_5_TURBO.getCode();

    /**
     * 问题描述
     */
    private List<Message> messages;
    /**
     * 控制温度【随机性】；0到2之间。较高的值(如0.8)将使输出更加随机，而较低的值(如0.2)将使输出更加集中和确定
     */
    @Builder.Default
    private double temperature = 0.2;
    /**
     * 多样性控制；使用温度采样的替代方法称为核心采样，其中模型考虑具有top_p概率质量的令牌的结果。因此，0.1 意味着只考虑包含前 10% 概率质量的代币
     */
    @JsonProperty("top_p")
    @Builder.Default
    private Double topP = 1d;
    /**
     * 为每个提示生成的完成次数
     */
    @Builder.Default
    private Integer n = 1;
    /**
     * 是否为流式输出
     */
    @Builder.Default
    private boolean stream = false;
    /**
     * 停止输出标识
     */
    private List<String> stop;
    /**
     * 输出字符串限制；0 ~ 4096
     */
    @JsonProperty("max_tokens")
    @Builder.Default
    private Integer maxTokens = 2048;
    /**
     * 频率惩罚；降低模型重复同一行的可能性
     */
    @JsonProperty("frequency_penalty")
    @Builder.Default
    private double frequencyPenalty = 0;
    /**
     * 存在惩罚；增强模型谈论新话题的可能性
     */
    @JsonProperty("presence_penalty")
    @Builder.Default
    private double presencePenalty = 0;
    /**
     * 生成多个调用结果，只显示最佳的。这样会更多的消耗你的 api token
     */
    @JsonProperty("logit_bias")
    private Map logitBias;
    /**
     * 调用标识，避免重复调用
     */
    private String user;

    @Getter
    @AllArgsConstructor
    public enum Role {

        SYSTEM("system"),
        USER("user"),
        ASSISTANT("assistant"),
        MODEL("model"),


        USER_INFO("user_info"),
        BOT_INFO("bot_info"),
        BOT_NAME("bot_name"),
        USER_NAME("user_name"),
        ;

        private final String code;

    }

    @Getter
    @AllArgsConstructor
    public enum Model {
        /**
         * gpt-3.5-turbo
         */
        GPT_3_5_TURBO("gpt-3.5-turbo"),
        GPT_3_5_TURBO_1106("gpt-3.5-turbo-1106"),

        // ChatGLM
        CHAT_GLM_4_FLASH("glm-4-flash"),


        // Spark
        SPARK_GENERAL("general"),
        ULTRA_4("4.0Ultra"),

        // Ali
        QWEN_MAX("qwen-max"),
        QWEN_VL_MAX_0809("qwen-vl-max-0809"),
        QWEN_TURBO("qwen-turbo"),



        ;

        private final String code;
    }
 }
