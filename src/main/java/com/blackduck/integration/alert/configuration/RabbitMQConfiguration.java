/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.configuration;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQConfiguration {
    public static final int BACKOFF_MULTIPLIER = 2;
    public static final int BACKOFF_INITIAL_INTERVAL = 5000; // 5 seconds
    public static final int BACKOFF_MAX_INTERVAL = 60000; // 1 minute

    public static final String EXCHANGE_NAME = "alert-queue-exchange";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "alert-dead-letter-exchange";

    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }

    @Bean
    public RetryTemplate rabbitmqRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setMultiplier(BACKOFF_MULTIPLIER);
        backOffPolicy.setInitialInterval(BACKOFF_INITIAL_INTERVAL);
        backOffPolicy.setMaxInterval(BACKOFF_MAX_INTERVAL);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(new AlwaysRetryPolicy());
        return retryTemplate;
    }
}
