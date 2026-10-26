/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.retry.support.RetryTemplate;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import com.blackduck.integration.alert.api.event.DeadLetterListener;
import com.blackduck.integration.alert.test.common.TestTags;

@Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
class EventListenerConfigurerTestIT {

    private static final RabbitMQContainer RABBIT_MQ_TEST_CONTAINER = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.3"));

    @BeforeAll
    static void startContainer() {
        RABBIT_MQ_TEST_CONTAINER.start();
    }

    @AfterAll
    static void stopContainer() {
        RABBIT_MQ_TEST_CONTAINER.stop();
    }

    private CachingConnectionFactory connectionFactory;
    private RabbitAdmin rabbitAdmin;
    private EventListenerConfigurer eventListenerConfigurer;

    @BeforeEach
    void setUp() {
        connectionFactory = new CachingConnectionFactory(RABBIT_MQ_TEST_CONTAINER.getHost(), RABBIT_MQ_TEST_CONTAINER.getAmqpPort());
        connectionFactory.setUsername(RABBIT_MQ_TEST_CONTAINER.getAdminUsername());
        connectionFactory.setPassword(RABBIT_MQ_TEST_CONTAINER.getAdminPassword());

        rabbitAdmin = new RabbitAdmin(connectionFactory);

        RabbitProperties rabbitProperties = Mockito.mock(RabbitProperties.class);
        RabbitProperties.Listener listener = Mockito.mock(RabbitProperties.Listener.class);
        RabbitProperties.SimpleContainer simpleContainer = Mockito.mock(RabbitProperties.SimpleContainer.class);
        Mockito.when(rabbitProperties.getListener()).thenReturn(listener);
        Mockito.when(listener.getSimple()).thenReturn(simpleContainer);

        eventListenerConfigurer = new EventListenerConfigurer(
            List.of(),
            List.of(),
            List.of(),
            connectionFactory,
            Mockito.mock(RetryTemplate.class),
            rabbitAdmin,
            Mockito.mock(TopicExchange.class),
            Mockito.mock(DeadLetterListener.class),
            Mockito.mock(RabbitTemplate.class),
            rabbitProperties,
            Mockito.mock(SimpleRabbitListenerContainerFactoryConfigurer.class)
        );
    }

    @AfterEach
    void tearDown() {
        rabbitAdmin.deleteQueue(DeadLetterListener.DEAD_LETTER_QUEUE_NAME);
        connectionFactory.destroy();
    }

    @Test
    void declareQueueWithMigrationWhenNoExistingQueueSucceeds() {
        Queue queue = QueueBuilder.durable(DeadLetterListener.DEAD_LETTER_QUEUE_NAME).build();

        assertDoesNotThrow(() -> eventListenerConfigurer.declareQueueWithMigration(queue), "Declaration should succeed when queue does not exist");

        Properties queueProperties = rabbitAdmin.getQueueProperties(DeadLetterListener.DEAD_LETTER_QUEUE_NAME);
        assertNotNull(queueProperties, "Queue should exist after declaration");
    }

    @Test
    void declareQueueWithMigrationWhenQueueExistsWithIncompatibleArgsMigratesQueue() {
        // Declare a queue with x-expires to simulate a queue created by an older version of Alert
        Queue oldQueue = QueueBuilder.durable(DeadLetterListener.DEAD_LETTER_QUEUE_NAME)
            .withArgument("x-expires", 28800000)
            .build();
        rabbitAdmin.declareQueue(oldQueue);

        Queue newQueue = QueueBuilder.durable(DeadLetterListener.DEAD_LETTER_QUEUE_NAME).build();

        assertDoesNotThrow(() -> eventListenerConfigurer.declareQueueWithMigration(newQueue), "Migration should succeed when queue has incompatible arguments");

        Properties queueProperties = rabbitAdmin.getQueueProperties(DeadLetterListener.DEAD_LETTER_QUEUE_NAME);
        assertNotNull(queueProperties, "Queue should exist after migration");

        // Use the broker to redeclare the queue. If "x-expires" was not removed this declaration will fail with a PRECONDITION_FAILED mismatch error.
        assertDoesNotThrow(() -> rabbitAdmin.declareQueue(newQueue), "Migrated queue should accept declaration without 'x-expires' set");
    }

    @Test
    void declareQueueWithMigrationWhenQueueExistsWithCompatibleArgsSucceeds() {
        Queue queue = QueueBuilder.durable(DeadLetterListener.DEAD_LETTER_QUEUE_NAME).build();
        rabbitAdmin.declareQueue(queue);

        // Re-declaring an identical queue should not produce an error.
        assertDoesNotThrow(() -> eventListenerConfigurer.declareQueueWithMigration(queue), "Re-declaring a compatible queue should succeed");
    }

}

