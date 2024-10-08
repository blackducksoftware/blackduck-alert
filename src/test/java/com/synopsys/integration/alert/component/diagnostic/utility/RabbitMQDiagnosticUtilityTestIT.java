package com.synopsys.integration.alert.component.diagnostic.utility;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackduck.integration.alert.component.diagnostic.utility.RabbitMQDiagnosticUtility;
import com.synopsys.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.blackduck.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

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
            .collect(Collectors.toList());

        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = rabbitMQDiagnosticUtility.getRabbitMQDiagnostics();
        assertTrue(rabbitMQDiagnosticModel.getQueues().size() > 0);

        List<String> queueNames = rabbitMQDiagnosticModel.getQueues()
            .stream()
            .map(AlertQueueInformation::getName)
            .collect(Collectors.toList());
        
        assertTrue(messageListenerNames.containsAll(queueNames));
    }
}
