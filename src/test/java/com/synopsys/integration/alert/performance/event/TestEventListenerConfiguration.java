package com.synopsys.integration.alert.performance.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;

@Configuration
public class TestEventListenerConfiguration {

    @Bean
    public TestAlertEventListener testEventListener1(Gson gson, TaskExecutor taskExecutor) {
        return new TestAlertEventListener(gson, taskExecutor, TestAlertEventHandler.DESTINATION_NAME + "_1", new TestAlertEventHandler());
    }

    @Bean
    public TestAlertEventListener testEventListener2(Gson gson, TaskExecutor taskExecutor) {
        return new TestAlertEventListener(gson, taskExecutor, TestAlertEventHandler.DESTINATION_NAME + "_2", new TestAlertEventHandler());
    }

    @Bean
    public TestAlertEventListener testEventListener3(Gson gson, TaskExecutor taskExecutor) {
        return new TestAlertEventListener(gson, taskExecutor, TestAlertEventHandler.DESTINATION_NAME + "_3", new TestAlertEventHandler());
    }

    @Bean
    public TestAlertEventListener testEventListener4(Gson gson, TaskExecutor taskExecutor) {
        return new TestAlertEventListener(gson, taskExecutor, TestAlertEventHandler.DESTINATION_NAME + "_4", new TestAlertEventHandler());
    }

    @Bean
    public TestAlertEventListener testEventListener5(Gson gson, TaskExecutor taskExecutor) {
        return new TestAlertEventListener(gson, taskExecutor, TestAlertEventHandler.DESTINATION_NAME + "_5", new TestAlertEventHandler());
    }
}
