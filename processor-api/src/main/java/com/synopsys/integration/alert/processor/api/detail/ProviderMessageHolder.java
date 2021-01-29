/*
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
package com.synopsys.integration.alert.processor.api.detail;

import java.util.List;

import org.apache.commons.collections4.ListUtils;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class ProviderMessageHolder extends AlertSerializableModel {
    private final List<ProjectMessage> projectMessages;
    private final List<SimpleMessage> simpleMessages;

    public static ProviderMessageHolder empty() {
        return new ProviderMessageHolder(List.of(), List.of());
    }

    public static ProviderMessageHolder reduce(ProviderMessageHolder lhs, ProviderMessageHolder rhs) {
        List<ProjectMessage> unifiedProjectMessages = ListUtils.union(lhs.getProjectMessages(), rhs.getProjectMessages());
        List<SimpleMessage> unifiedSimpleMessages = ListUtils.union(lhs.getSimpleMessages(), rhs.getSimpleMessages());
        return new ProviderMessageHolder(unifiedProjectMessages, unifiedSimpleMessages);
    }

    public ProviderMessageHolder(List<ProjectMessage> projectMessages, List<SimpleMessage> simpleMessages) {
        this.projectMessages = projectMessages;
        this.simpleMessages = simpleMessages;
    }

    public List<ProjectMessage> getProjectMessages() {
        return projectMessages;
    }

    public List<SimpleMessage> getSimpleMessages() {
        return simpleMessages;
    }

}
