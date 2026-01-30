/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.configuration;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

import com.blackduck.integration.alert.api.channel.DistributionEventReceiver;
import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.api.event.DeadLetterListener;
import com.blackduck.integration.alert.api.processor.event.NotificationProcessingReceiver;

@Configuration
public class EventListenerConfigurer implements RabbitListenerConfigurer {
    private final Logger logger = LoggerFactory.getLogger(EventListenerConfigurer.class);

    private final List<AlertMessageListener<?>> allAlertMessageListeners;
    private final Set<String> distributionEventDestinationNames;
    private final Set<String> processingEventDestinationNames;
    private final CachingConnectionFactory cachingConnectionFactory;
    private final RetryTemplate rabbitmqRetryTemplate;
    private final AmqpAdmin amqpAdmin;
    private final TopicExchange exchange;
    private final DeadLetterListener deadLetterListener;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties rabbitProperties;
    private final SimpleRabbitListenerContainerFactoryConfigurer rabbitListenerContainerFactoryConfigurer;

    @Autowired
    public EventListenerConfigurer(
        List<AlertMessageListener<?>> allAlertMessageListeners,
        List<DistributionEventReceiver<?>> distributionEventReceivers,
        List<NotificationProcessingReceiver<?>> processingEventReceivers,
        CachingConnectionFactory cachingConnectionFactory,
        RetryTemplate rabbitmqRetryTemplate,
        AmqpAdmin amqpAdmin,
        TopicExchange exchange,
        DeadLetterListener deadLetterListener,
        RabbitTemplate rabbitTemplate,
        RabbitProperties rabbitProperties,
        SimpleRabbitListenerContainerFactoryConfigurer rabbitListenerContainerFactoryConfigurer
    ) {
        this.allAlertMessageListeners = allAlertMessageListeners;
        this.distributionEventDestinationNames = distributionEventReceivers
            .stream()
            .map(AlertMessageListener::getDestinationName)
            .collect(Collectors.toSet());
        this.processingEventDestinationNames = processingEventReceivers.stream()
            .map(AlertMessageListener::getDestinationName)
            .collect(Collectors.toSet());
        this.cachingConnectionFactory = cachingConnectionFactory;
        this.rabbitmqRetryTemplate = rabbitmqRetryTemplate;
        this.amqpAdmin = amqpAdmin;
        this.exchange = exchange;
        this.deadLetterListener = deadLetterListener;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitProperties = rabbitProperties;
        this.rabbitListenerContainerFactoryConfigurer = rabbitListenerContainerFactoryConfigurer;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        logRabbitMqConfig();
        // setup persistence for the rabbit template
        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        // declare the main exchange before binding queues to it.
        amqpAdmin.declareExchange(exchange);
        createDeadLetterHandler(registrar);
        MessageListenerContainer alertDefaultMessageListenerContainer = createMessageListenerContainer();
        logger.debug("Registering JMS Listeners");
        for (AlertMessageListener<?> messageListener : allAlertMessageListeners) {
            String destinationName = messageListener.getDestinationName();
            boolean createMessageListenerContainer = distributionEventDestinationNames.contains(destinationName)
                || processingEventDestinationNames.contains(destinationName);
            if (createMessageListenerContainer) {
                MessageListenerContainer messageListenerContainer = createMessageListenerContainer();
                registerListenerEndpoint(registrar, messageListener, messageListenerContainer);
            } else {
                registerListenerEndpoint(registrar, messageListener, alertDefaultMessageListenerContainer);
            }
        }
    }

    private void logRabbitMqConfig() {
        logger.info("Rabbitmq connection details:");
        logger.info("  host:                   {}", cachingConnectionFactory.getHost());
        logger.info("  port:                   {}", cachingConnectionFactory.getPort());
        logger.info("  ssl enabled:            {}", cachingConnectionFactory.getRabbitConnectionFactory().isSSL());
        logger.info("  cache mode:             {}", cachingConnectionFactory.getCacheMode());
        logger.info("  connection cache size:  {}", cachingConnectionFactory.getConnectionCacheSize());
        logger.info("  channel cache size:     {}", cachingConnectionFactory.getChannelCacheSize());

        if (StringUtils.isNotBlank(cachingConnectionFactory.getUsername())) {
            logger.info("  username:               *******");
        } else {
            logger.info("  username: ");
        }
        if (StringUtils.isNotBlank(cachingConnectionFactory.getRabbitConnectionFactory().getPassword())) {
            logger.info("  password:               *******");
        } else {
            logger.info("  password: ");
        }

        logger.info("  vhost:                  {}", cachingConnectionFactory.getVirtualHost());
        logger.info("Rabbitmq Exchange details:");
        logger.info("  Name:    {}", exchange.getName());
        logger.info("  Type:    {}", exchange.getType());
        logger.info("  Durable: {}", exchange.isDurable());
        logger.info("Rabbitmq Simple Listener Container details: ");
        RabbitProperties.SimpleContainer simpleContainerProperties = rabbitProperties.getListener().getSimple();
        logger.info("  Concurrency:            {}", simpleContainerProperties.getConcurrency());
        logger.info("  Max Concurrency:        {}", simpleContainerProperties.getMaxConcurrency());
        logger.info("  Prefetch:               {}", simpleContainerProperties.getPrefetch());
        logger.info("  Acknowledge Mode:       {}", simpleContainerProperties.getAcknowledgeMode());
        logger.info("  Missing Queues Fatal:   {}", simpleContainerProperties.isMissingQueuesFatal());
    }

    private void createDeadLetterHandler(RabbitListenerEndpointRegistrar registrar) {
        logger.debug("Registering dead letter listener");
        org.springframework.amqp.rabbit.listener.MessageListenerContainer deadLetterListenerContainer = createMessageListenerContainer();
        amqpAdmin.declareExchange(exchange);
        String listenerId = createListenerId(DeadLetterListener.DEAD_LETTER_QUEUE_NAME);
        Queue queue = QueueBuilder
            .durable(DeadLetterListener.DEAD_LETTER_QUEUE_NAME)
            .withArgument("x-expires", TimeUnit.HOURS.toMillis(8))
            // may need to add dead letter queue and dead letter queue listener
            .build();
        TopicExchange deadLetterExchange = ExchangeBuilder.topicExchange(RabbitMQConfiguration.DEAD_LETTER_EXCHANGE_NAME).durable(true).build();
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
        rabbitListenerContainerFactoryConfigurer.configure(containerFactory, cachingConnectionFactory);
        containerFactory.setRetryTemplate(rabbitmqRetryTemplate);
        containerFactory.setFailedDeclarationRetryInterval(30000L); // default is 5 seconds
        return containerFactory.createListenerContainer();
    }

    private void registerListenerEndpoint(RabbitListenerEndpointRegistrar registrar, AlertMessageListener<?> listener, MessageListenerContainer messageListenerContainer) {
        String destinationName = listener.getDestinationName();
        String listenerId = createListenerId(destinationName);
        initQueue(destinationName);
        logger.debug("Registering Listener: {}", listenerId);
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
