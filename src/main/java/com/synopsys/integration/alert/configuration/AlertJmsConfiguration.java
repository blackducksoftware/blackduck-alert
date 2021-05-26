/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

@Configuration
public class AlertJmsConfiguration {
    // We want to create a upper limit of threads so Alert does not attempt to take all available threads in a customers environment.
    private final int MAX_ALLOWED_THREAD_COUNT = 10;

    @Bean
    public DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory(CachingConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory distributionChannelJmsListenerContainerFactory(CachingConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        int minConcurrency = 1;
        int maxThreadCount = Runtime.getRuntime().availableProcessors();
        int maxConcurrency = Math.min(maxThreadCount, MAX_ALLOWED_THREAD_COUNT);
        String concurrencyRange = String.format("%s-%s", minConcurrency, maxConcurrency);
        factory.setConcurrency(concurrencyRange);

        return factory;
    }

}
