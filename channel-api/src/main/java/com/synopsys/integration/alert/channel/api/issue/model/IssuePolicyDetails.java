/*
 * channel-api
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
package com.synopsys.integration.alert.channel.api.issue.model;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
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
