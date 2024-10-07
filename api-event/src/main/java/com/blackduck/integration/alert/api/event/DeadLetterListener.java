/*
 * api-event
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.event;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = DeadLetterListener.DEAD_LETTER_QUEUE_NAME)
public class DeadLetterListener implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String DEAD_LETTER_QUEUE_NAME = "alert-dead-letter";

    @Override
    public void onMessage(Message message) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        StringBuilder logMessage = new StringBuilder(" [Queue name: ").append(headers.get("queue"))
            .append(", time: ").append(headers.get("time"))
            .append(", reason: ").append(headers.getOrDefault("reason", "N/A"))
            .append(", message: ").append(message.getBody()).append("].");

        logger.error("Dead letter message received {}", logMessage);
    }
}
