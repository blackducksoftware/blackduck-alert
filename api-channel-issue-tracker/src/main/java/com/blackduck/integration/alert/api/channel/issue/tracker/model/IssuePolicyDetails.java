package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;

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
