/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.diagnostic.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class AlertQueueInformation extends AlertSerializableModel {
    private static final long serialVersionUID = 5325498975996171650L;

    private final String name;
    private final int messageCount;
    private final int consumerCount;

    public AlertQueueInformation(String name, int messageCount, int consumerCount) {
        this.name = name;
        this.messageCount = messageCount;
        this.consumerCount = consumerCount;
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
}
