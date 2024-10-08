/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.utility;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.blackduck.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;

@Component
public class RabbitMQDiagnosticUtility {
    private final AmqpAdmin amqpAdmin;
    private final Set<String> alertMessageListenerNames;

    @Autowired
    public RabbitMQDiagnosticUtility(AmqpAdmin amqpAdmin, List<AlertMessageListener<?>> allAlertMessageListeners) {
        this.amqpAdmin = amqpAdmin;
        this.alertMessageListenerNames = allAlertMessageListeners
            .stream()
            .map(AlertMessageListener::getDestinationName)
            .collect(Collectors.toSet());
    }

    public RabbitMQDiagnosticModel getRabbitMQDiagnostics() {
        List<AlertQueueInformation> alertQueueInformation = alertMessageListenerNames
            .stream()
            .map(amqpAdmin::getQueueInfo)
            .filter(Objects::nonNull)
            .map(queueInfo -> new AlertQueueInformation(queueInfo.getName(), queueInfo.getMessageCount(), queueInfo.getConsumerCount()))
            .collect(Collectors.toList());
        return new RabbitMQDiagnosticModel(alertQueueInformation);
    }
}
