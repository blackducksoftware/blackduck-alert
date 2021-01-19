/**
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.extract.model.CombinableModel;

public class ComponentConcern extends AlertSerializableModel implements CombinableModel<ComponentConcern> {
    private final ComponentConcernType type;
    private final ItemOperation operation;
    private final String name;
    private final String severity;
    private final String url;

    public static ComponentConcern policy(ItemOperation operation, String policyName) {
        return new ComponentConcern(ComponentConcernType.POLICY, operation, policyName, null, null);
    }

    public static ComponentConcern severePolicy(ItemOperation operation, String policyName, String severity) {
        return new ComponentConcern(ComponentConcernType.POLICY, operation, policyName, severity, null);
    }

    public static ComponentConcern vulnerability(ItemOperation operation, String vulnerabilityId, String severity, String vulnerabilityUrl) {
        return new ComponentConcern(ComponentConcernType.VULNERABILITY, operation, vulnerabilityId, severity, vulnerabilityUrl);
    }

    private ComponentConcern(ComponentConcernType type, ItemOperation operation, String name, @Nullable String severity, @Nullable String url) {
        this.operation = operation;
        this.type = type;
        this.name = name;
        this.severity = severity;
        this.url = url;
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

    public Optional<String> getSeverity() {
        return Optional.ofNullable(severity);
    }

    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
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
            return List.of();
        } else {
            return uncombinedModels;
        }
    }

}
