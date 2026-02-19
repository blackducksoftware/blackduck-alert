/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.utility;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.blackduck.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.blackduck.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class RabbitMQDiagnosticUtilityTestIT {
    @Autowired
    private RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility;
    @Autowired
    private List<AlertMessageListener<?>> allAlertMessageListeners;

    @Test
    void getRabbitMQDiagnosticsTest() {
        List<String> messageListenerNames = allAlertMessageListeners.stream()
            .map(AlertMessageListener::getDestinationName)
            .toList();

        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = rabbitMQDiagnosticUtility.getRabbitMQDiagnostics();
        assertFalse(rabbitMQDiagnosticModel.getQueues().isEmpty());

        List<String> queueNames = rabbitMQDiagnosticModel.getQueues()
            .stream()
            .map(AlertQueueInformation::getName)
            .toList();
        
        assertTrue(messageListenerNames.containsAll(queueNames));
    }
}
