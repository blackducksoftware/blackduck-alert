package com.blackduck.integration.alert.common.rest.api;

// Javadoc of Runnable implies running in a thread and being executed asynchronously.
@FunctionalInterface
public interface Procedure {
    void run();
}
