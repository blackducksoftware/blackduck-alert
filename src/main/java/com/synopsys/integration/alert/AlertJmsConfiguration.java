/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert;

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
