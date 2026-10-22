/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class RabbitMQConfigurationRetryTest {
    private RabbitMQConfiguration rabbitMQConfiguration;
    private RetryTemplate retryTemplate;

    @BeforeEach
    void setUp() {
        rabbitMQConfiguration = new RabbitMQConfiguration();
        retryTemplate = rabbitMQConfiguration.rabbitmqRetryTemplate();
    }

    @Test
    void testRabbitmqRetryTemplateBeanConfiguration() {
        assertNotNull(retryTemplate, "RetryTemplate bean should not be null");
        RetryPolicy retryPolicy = retryTemplate.getRetryPolicy();
        assertNotNull(retryPolicy, "RetryPolicy should not be null");

        BackOff backOff = retryPolicy.getBackOff();
        assertNotNull(backOff, "BackOff policy should be configured");
        assertTrue(backOff instanceof ExponentialBackOff, "BackOff must be an ExponentialBackOff");

        ExponentialBackOff expBackOff = (ExponentialBackOff) backOff;
        assertEquals(RabbitMQConfiguration.BACKOFF_INITIAL_INTERVAL, expBackOff.getInitialInterval());
        assertEquals(RabbitMQConfiguration.BACKOFF_MULTIPLIER, expBackOff.getMultiplier());
        assertEquals(RabbitMQConfiguration.BACKOFF_MAX_INTERVAL, expBackOff.getMaxInterval());
        assertEquals((long) Integer.MAX_VALUE, expBackOff.getMaxAttempts());
    }

    @Test
    void testExponentialBackOffProgression() {
        RetryPolicy retryPolicy = retryTemplate.getRetryPolicy();
        BackOff backOff = retryPolicy.getBackOff();
        BackOffExecution execution = backOff.start();

        long expectedDelay = RabbitMQConfiguration.BACKOFF_INITIAL_INTERVAL;
        long maxInterval = RabbitMQConfiguration.BACKOFF_MAX_INTERVAL;

        for (int i = 0; i < 7; i++) {
            assertEquals(expectedDelay, execution.nextBackOff());
            expectedDelay = Math.min(expectedDelay * RabbitMQConfiguration.BACKOFF_MULTIPLIER, maxInterval);
        }
    }

    @Test
    void testRetryPolicyRetriesOnExceptionsLikeAlwaysRetryPolicy() {
        RetryPolicy retryPolicy = retryTemplate.getRetryPolicy();

        assertTrue(retryPolicy.shouldRetry(new RuntimeException("Transient failure")));
        assertTrue(retryPolicy.shouldRetry(new IOException("Broker unreachable")));
        assertTrue(retryPolicy.shouldRetry(new IllegalStateException("Listener error")));
        assertTrue(retryPolicy.shouldRetry(new Exception("Checked exception")));
    }

    @Test
    void testSuccessfulExecutionWithoutRetry() throws RetryException {
        AtomicInteger attemptCount = new AtomicInteger(0);

        String result = retryTemplate.execute(() -> {
            attemptCount.incrementAndGet();
            return "SUCCESS";
        });

        assertEquals("SUCCESS", result);
        assertEquals(1, attemptCount.get());
    }

    @Test
    void testExecutionRecoversAfterFailures() throws RetryException {
        AtomicInteger attempts = new AtomicInteger(0);
        int succeedOnAttempt = 2;

        String result = retryTemplate.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < succeedOnAttempt) {
                throw new IOException("Simulated transient connection error at attempt " + attempt);
            }
            return "RECOVERED";
        });

        assertEquals("RECOVERED", result);
        assertEquals(succeedOnAttempt, attempts.get());
    }
}