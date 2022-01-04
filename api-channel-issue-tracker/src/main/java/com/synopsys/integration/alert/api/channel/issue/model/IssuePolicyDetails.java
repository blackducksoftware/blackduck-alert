/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;

public class IssuePolicyDetails extends AlertSerializableModel {
    private final String name;
    private final ItemOperation operation;
    private final ComponentConcernSeverity severity;

    public IssuePolicyDetails(String name, ItemOperation operation, ComponentConcernSeverity severity) {
        this.name = name;
        this.operation = operation;
        this.severity = severity;
    }

    public String getName() {
        return name;
    }

    public ItemOperation getOperation() {
        return operation;
    }

    public ComponentConcernSeverity getSeverity() {
        return severity;
    }

}
