/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.configuration;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActiveMQConfiguration implements ActiveMQConnectionFactoryCustomizer {
    public static final String BROKER_SPLIT_MEMORY_QUERY_PARAM = "broker.splitSystemUsageForProducersConsumers=true";
    public static final int QUEUE_PREFETCH_LIMIT = 100;

    @Override
    public void customize(ActiveMQConnectionFactory factory) {
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();

        // setup consumer queue policy so that 100 events need to be acknowledged before send more events to the consumer.
        prefetchPolicy.setQueuePrefetch(QUEUE_PREFETCH_LIMIT);
        factory.setPrefetchPolicy(prefetchPolicy);
        String newBrokerUrl = createCustomBrokerUrl(factory);
        factory.setBrokerURL(newBrokerUrl);
    }

    private String createCustomBrokerUrl(ActiveMQConnectionFactory factory) {
        String brokerUrl = factory.getBrokerURL();
        StringBuilder urlBuilder = new StringBuilder(brokerUrl.length() + BROKER_SPLIT_MEMORY_QUERY_PARAM.length() + 1);
        urlBuilder.append(brokerUrl);
        if (brokerUrl.contains("?")) {
            urlBuilder.append("&");
        } else {
            urlBuilder.append("?");
        }
        urlBuilder.append(BROKER_SPLIT_MEMORY_QUERY_PARAM);
        return urlBuilder.toString();
    }

}
