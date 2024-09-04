
package com.tuto.openai.executor.parameter;

/**
 * 参数处理器
 * @param <T>
 */
public interface ParameterHandler<T> {

    T getParameterObject(CompletionRequest completionRequest);
}
