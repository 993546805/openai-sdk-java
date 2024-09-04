package com.tuto.openai.executor.model.spark.valobj;

import lombok.Builder;
import lombok.Data;

/**
 * @author tu
 * @date 2024-09-04 21:59
 */
@Data
@Builder
public class Message {
    private String role;
    private String content;
}
