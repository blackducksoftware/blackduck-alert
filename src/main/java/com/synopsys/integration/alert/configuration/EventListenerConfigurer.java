/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.configuration;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

import com.synopsys.integration.alert.api.channel.DistributionEventReceiver;
import com.synopsys.integration.alert.api.event.AlertMessageListener;
import com.synopsys.integration.alert.api.event.DeadLetterListener;

@Configuration
public class EventListenerConfigurer implements RabbitListenerConfigurer {
    private final Logger logger = LoggerFactory.getLogger(EventListenerConfigurer.class);

    private final List<AlertMessageListener<?>> allAlertMessageListeners;
    private final Set<String> distributionEventDestinationNames;
    private final CachingConnectionFactory cachingConnectionFactory;
    private final RetryTemplate rabbitmqRetryTemplate;
    private final AmqpAdmin amqpAdmin;
    private final TopicExchange exchange;
    private final DeadLetterListener deadLetterListener;

    @Autowired
    public EventListenerConfigurer(
        List<AlertMessageListener<?>> allAlertMessageListeners,
        List<DistributionEventReceiver<?>> distributionEventReceivers,
        CachingConnectionFactory cachingConnectionFactory,
        RetryTemplate rabbitmqRetryTemplate,
        AmqpAdmin amqpAdmin,
        TopicExchange exchange,
        DeadLetterListener deadLetterListener
    ) {
        this.allAlertMessageListeners = allAlertMessageListeners;
        this.distributionEventDestinationNames = distributionEventReceivers
            .stream()
            .map(AlertMessageListener::getDestinationName)
            .collect(Collectors.toSet());
        this.cachingConnectionFactory = cachingConnectionFactory;
        this.rabbitmqRetryTemplate = rabbitmqRetryTemplate;
        this.amqpAdmin = amqpAdmin;
        this.exchange = exchange;
        this.deadLetterListener = deadLetterListener;

    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        createDeadLetterHandler(registrar);
        org.springframework.amqp.rabbit.listener.MessageListenerContainer alertDefaultMessageListenerContainer = createMessageListenerContainer();
        logger.debug("Registering JMS Listeners");
        for (AlertMessageListener<?> messageListener : allAlertMessageListeners) {
            if (distributionEventDestinationNames.contains(messageListener.getDestinationName())) {
                org.springframework.amqp.rabbit.listener.MessageListenerContainer distributionChannelMessageListenerContainer = createMessageListenerContainer();
                registerListenerEndpoint(registrar, messageListener, distributionChannelMessageListenerContainer);
            } else {
                registerListenerEndpoint(registrar, messageListener, alertDefaultMessageListenerContainer);
            }
        }
    }

    private void createDeadLetterHandler(RabbitListenerEndpointRegistrar registrar) {
        logger.debug("Registering dead letter listener");
        org.springframework.amqp.rabbit.listener.MessageListenerContainer deadLetterListenerContainer = createMessageListenerContainer();
        String listenerId = createListenerId(DeadLetterListener.DEAD_LETTER_QUEUE_NAME);
        Queue queue = QueueBuilder
            .durable(DeadLetterListener.DEAD_LETTER_QUEUE_NAME)
            .withArgument("x-expires", TimeUnit.HOURS.toMillis(8))
            // may need to add dead letter queue and dead letter queue listener
            .build();
        TopicExchange deadLetterExchange = ExchangeBuilder.topicExchange(RabbitMQConfiguration.DEAD_LETTER_EXCHANGE_NAME).build();
        Binding binding = BindingBuilder.bind(queue).to(deadLetterExchange).with(DeadLetterListener.DEAD_LETTER_QUEUE_NAME);
        amqpAdmin.declareExchange(deadLetterExchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);
        logger.debug("Registering Dead Letter Listener: {}", listenerId);
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setId(listenerId);
        endpoint.setQueueNames(DeadLetterListener.DEAD_LETTER_QUEUE_NAME);
        endpoint.setMessageListener(deadLetterListener);
        endpoint.setupListenerContainer(deadLetterListenerContainer);
        registrar.registerEndpoint(endpoint);
    }

    private MessageListenerContainer createMessageListenerContainer() {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(cachingConnectionFactory);
        containerFactory.setRetryTemplate(rabbitmqRetryTemplate);
        containerFactory.setFailedDeclarationRetryInterval(30000L); // default is 5 seconds
        return containerFactory.createListenerContainer();
    }

    private void registerListenerEndpoint(RabbitListenerEndpointRegistrar registrar, AlertMessageListener<?> listener, MessageListenerContainer messageListenerContainer) {
        String destinationName = listener.getDestinationName();
        String listenerId = createListenerId(destinationName);
        initQueue(destinationName);
        logger.debug("Registering JMS Listener: {}", listenerId);
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setId(listenerId);
        endpoint.setQueueNames(destinationName);
        endpoint.setMessageListener(listener);
        endpoint.setupListenerContainer(messageListenerContainer);
        registrar.registerEndpoint(endpoint);
    }

    private void initQueue(String queueName) {
        Queue queue = QueueBuilder
            .durable(queueName)
            .withArgument("x-expires", TimeUnit.HOURS.toMillis(1))
            .withArgument("x-dead-letter-exchange", RabbitMQConfiguration.DEAD_LETTER_EXCHANGE_NAME)
            .withArgument("x-dead-letter-routing-key", DeadLetterListener.DEAD_LETTER_QUEUE_NAME)
            // may need to add dead letter queue and dead letter queue listener
            .build();
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(queueName);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);
    }

    private String createListenerId(String name) {
        return String.format("%sListener", name);
    }

}
