/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.CombinableModel;

public class ComponentConcern extends AlertSerializableModel implements CombinableModel<ComponentConcern>, Comparable<ComponentConcern> {
    private final ComponentConcernType type;
    private final ItemOperation operation;
    private final String name;
    private final ComponentConcernSeverity severity;
    private final String url;
    private final Number numericValue;

    public static ComponentConcern policy(ItemOperation operation, String policyName, String url) {
        return new ComponentConcern(ComponentConcernType.POLICY, operation, policyName, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, url);
    }

    public static ComponentConcern severePolicy(ItemOperation operation, String policyName, ComponentConcernSeverity severity, String url) {
        return new ComponentConcern(ComponentConcernType.POLICY, operation, policyName, severity, url);
    }

    public static ComponentConcern vulnerability(ItemOperation operation, String vulnerabilityId, ComponentConcernSeverity severity, String vulnerabilityUrl) {
        return new ComponentConcern(ComponentConcernType.VULNERABILITY, operation, vulnerabilityId, severity, vulnerabilityUrl);
    }

    public static ComponentConcern unknownComponentVersion(ItemOperation operation, String componentName, ComponentConcernSeverity severity, Number count, String url) {
        return new ComponentConcern(ComponentConcernType.UNKNOWN_VERSION, operation, componentName, severity, url, count);
    }

    private ComponentConcern(ComponentConcernType type, ItemOperation operation, String name, ComponentConcernSeverity severity, @Nullable String url) {
        this(type, operation, name, severity, url, null);
    }

    private ComponentConcern(ComponentConcernType type, ItemOperation operation, String name, ComponentConcernSeverity severity, @Nullable String url, @Nullable Number numericValue) {
        this.operation = operation;
        this.type = type;
        this.name = name;
        this.severity = severity;
        this.url = url;
        this.numericValue = numericValue;
    }

    public ComponentConcernType getType() {
        return type;
    }

    public ItemOperation getOperation() {
        return operation;
    }

    public String getName() {
        return name;
    }

    public ComponentConcernSeverity getSeverity() {
        return severity;
    }

    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    public boolean hasNumericValue() {
        return numericValue != null;
    }

    public Number getNumericValue() {
        return numericValue;
    }

    @Override
    public List<ComponentConcern> combine(ComponentConcern otherModel) {
        List<ComponentConcern> uncombinedModels = List.of(this, otherModel);

        if (!type.equals(otherModel.type) || !name.equals(otherModel.name)) {
            return uncombinedModels;
        }

        if (operation.equals(otherModel.operation)) {
            return List.of(this);
        } else if (ItemOperation.ADD.equals(operation) && ItemOperation.DELETE.equals(otherModel.operation)) {
            return List.of();
        } else if (ItemOperation.DELETE.equals(operation) && ItemOperation.ADD.equals(otherModel.operation)) {
            return List.of(otherModel);
        } else {
            return uncombinedModels;
        }
    }

    @Override
    public int compareTo(ComponentConcern otherModel) {
        if (!type.equals(otherModel.type)) {
            return type.compareTo(otherModel.type);
        }

        if (!operation.equals(otherModel.operation)) {
            return operation.compareTo(otherModel.operation);
        }

        if (!severity.equals(otherModel.severity)) {
            return severity.compareTo(otherModel.severity);
        }

        if (!name.equals(otherModel.name)) {
            return name.compareTo(otherModel.name);
        }

        return 0;
    }

}
