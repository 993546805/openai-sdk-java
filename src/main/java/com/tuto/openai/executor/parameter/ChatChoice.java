
package com.tuto.openai.executor.parameter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatChoice implements Serializable {


    private long index;
    /**
     * stream = true 请求参数里返回的属性是 delta
     */
    @JsonProperty("delta")
    private Message delta;
    /**
     * stream = false 请求参数里返回的属性是 delta
     */
    @JsonProperty("message")
    private Message message;
    @JsonProperty("finish_reason")
    private String finishReason;

}
