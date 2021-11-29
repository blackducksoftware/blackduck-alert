package com.synopsys.integration.alert.performance.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestEventListenerConfiguration {

    @Bean
    public TestEventListener testEventListener1() {
        return new TestEventListener(TestEventListener.DESTINATION_NAME + "_1");
    }

    @Bean
    public TestEventListener testEventListener2() {
        return new TestEventListener(TestEventListener.DESTINATION_NAME + "_2");
    }

    @Bean
    public TestEventListener testEventListener3() {
        return new TestEventListener(TestEventListener.DESTINATION_NAME + "_3");
    }

    @Bean
    public TestEventListener testEventListener4() {
        return new TestEventListener(TestEventListener.DESTINATION_NAME + "_4");
    }

    @Bean
    public TestEventListener testEventListener5() {
        return new TestEventListener(TestEventListener.DESTINATION_NAME + "_5");
    }
}
