package com.tuto.openai.executor.model.chatglm.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ChatGLM 消息模型
 *
 * @author tu
 */
@Getter
@AllArgsConstructor
public enum Model {

    CHAT_GLM_4_FLASH("glm-4-flash", "ChatGLM-4-Flash 模型"),
    CHATGLM_6B_SSE("chatGLM_6b_SSE", "ChatGLM-6B 测试模型"),
    CHATGLM_LITE("chatglm_lite", "轻量版模型，适用对推理速度和成本敏感的场景"),
    CHATGLM_LITE_32K("chatglm_lite_32k", "标准版模型，适用兼顾效果和成本的场景"),
    CHATGLM_STD("chatglm_std", "适用于对知识量、推理能力、创造力要求较高的场景"),
    CHATGLM_PRO("chatglm_pro", "适用于对知识量、推理能力、创造力要求较高的场景"),

    ;
    private final String code;
    private final String info;

    public static Model getModel(String code) {
        for (Model model : values()) {
            if (model.code.equals(code)) {
                return model;
            }
        }
        return CHAT_GLM_4_FLASH;
    }
}
