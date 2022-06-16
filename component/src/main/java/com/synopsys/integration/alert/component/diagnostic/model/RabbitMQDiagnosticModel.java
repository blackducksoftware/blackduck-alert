/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.diagnostic.model;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class RabbitMQDiagnosticModel extends AlertSerializableModel {
    private static final long serialVersionUID = 1416746180446555818L;
    private List<AlertQueueInformation> queues;

    public RabbitMQDiagnosticModel() {
        // For serialization
    }

    public RabbitMQDiagnosticModel(List<AlertQueueInformation> queues) {
        this.queues = queues;
    }

    public List<AlertQueueInformation> getQueues() {
        return queues;
    }
}
