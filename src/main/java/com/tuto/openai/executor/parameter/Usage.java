package com.tuto.openai.executor.parameter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.io.Serializable;

/**
 * 使用量
 */
@Setter
public class Usage implements Serializable {

    /**
     * 提示令牌
     */
    @JsonProperty("prompt_tokens")
    private long promptTokens;
    /**
     * 完成令牌
     */
    @JsonProperty("completion_tokens")
    private long completionTokens;
    /**
     * 总量令牌
     */
    @JsonProperty("total_tokens")
    private long totalTokens;

}
