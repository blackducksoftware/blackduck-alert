package com.synopsys.integration.alert.common.function;

// TODO add this to integration-common
public interface ThrowingFunction<T, R, E extends Throwable> {
    /**
     * Applies this function, which may throw an exception, to the given argument.
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws E;

}
