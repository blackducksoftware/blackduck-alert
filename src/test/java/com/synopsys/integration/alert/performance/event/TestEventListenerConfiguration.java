package com.synopsys.integration.alert.performance.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestEventListenerConfiguration {

    @Bean
    public TestAlertEventListener testEventListener1() {
        return new TestAlertEventListener(TestAlertEventListener.DESTINATION_NAME + "_1");
    }

    @Bean
    public TestAlertEventListener testEventListener2() {
        return new TestAlertEventListener(TestAlertEventListener.DESTINATION_NAME + "_2");
    }

    @Bean
    public TestAlertEventListener testEventListener3() {
        return new TestAlertEventListener(TestAlertEventListener.DESTINATION_NAME + "_3");
    }

    @Bean
    public TestAlertEventListener testEventListener4() {
        return new TestAlertEventListener(TestAlertEventListener.DESTINATION_NAME + "_4");
    }

    @Bean
    public TestAlertEventListener testEventListener5() {
        return new TestAlertEventListener(TestAlertEventListener.DESTINATION_NAME + "_5");
    }
}
