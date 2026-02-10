/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.utility;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.blackduck.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;

@Component
public class RabbitMQDiagnosticUtility {
    private final AmqpAdmin amqpAdmin;
    private final List<AlertMessageListener<?>> alertMessageListeners;

    @Autowired
    public RabbitMQDiagnosticUtility(AmqpAdmin amqpAdmin, List<AlertMessageListener<?>> allAlertMessageListeners) {
        this.amqpAdmin = amqpAdmin;
        this.alertMessageListeners = allAlertMessageListeners;
    }

    public RabbitMQDiagnosticModel getRabbitMQDiagnostics() {
        List<AlertQueueInformation> alertQueueInformation = new LinkedList<>();
        for(AlertMessageListener<?> listener: alertMessageListeners) {
            QueueInformation queueInformation = amqpAdmin.getQueueInfo(listener.getDestinationName());
            if(Objects.nonNull(queueInformation)) {
                alertQueueInformation.add(new AlertQueueInformation(
                    queueInformation.getName(),
                    queueInformation.getMessageCount(),
                    queueInformation.getConsumerCount(),
                    listener.calculateAverageMessageSize()));
            }
        }
        return new RabbitMQDiagnosticModel(alertQueueInformation);
    }
}
