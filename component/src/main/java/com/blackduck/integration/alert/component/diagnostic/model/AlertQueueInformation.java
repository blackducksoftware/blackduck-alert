/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class AlertQueueInformation extends AlertSerializableModel {
    private static final long serialVersionUID = 5325498975996171650L;

    private final String name;
    private final int messageCount;
    private final int consumerCount;
    private final double averageMessageSize;

    public AlertQueueInformation(String name, int messageCount, int consumerCount, double averageMessageSize) {
        this.name = name;
        this.messageCount = messageCount;
        this.consumerCount = consumerCount;
        this.averageMessageSize = averageMessageSize;
    }

    public String getName() {
        return name;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public int getConsumerCount() {
        return consumerCount;
    }
    public double getAverageMessageSize() {
        return averageMessageSize;
    }
}
