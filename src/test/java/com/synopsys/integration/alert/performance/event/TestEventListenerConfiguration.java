package com.synopsys.integration.alert.performance.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;

@Configuration
public class TestEventListenerConfiguration {

    @Bean
    public TestAlertEventListener testEventListener1(Gson gson) {
        return new TestAlertEventListener(gson, TestAlertEventHandler.DESTINATION_NAME + "_1", new TestAlertEventHandler());
    }

    @Bean
    public TestAlertEventListener testEventListener2(Gson gson) {
        return new TestAlertEventListener(gson, TestAlertEventHandler.DESTINATION_NAME + "_2", new TestAlertEventHandler());
    }

    @Bean
    public TestAlertEventListener testEventListener3(Gson gson) {
        return new TestAlertEventListener(gson, TestAlertEventHandler.DESTINATION_NAME + "_3", new TestAlertEventHandler());
    }

    @Bean
    public TestAlertEventListener testEventListener4(Gson gson) {
        return new TestAlertEventListener(gson, TestAlertEventHandler.DESTINATION_NAME + "_4", new TestAlertEventHandler());
    }

    @Bean
    public TestAlertEventListener testEventListener5(Gson gson) {
        return new TestAlertEventListener(gson, TestAlertEventHandler.DESTINATION_NAME + "_5", new TestAlertEventHandler());
    }
}
