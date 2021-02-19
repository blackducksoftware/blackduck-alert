/*
 * channel
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
package com.synopsys.integration.alert.channel.jira2.common.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class JiraCustomFieldReplacementValues {
    // "None" is a frequently used default String for many Jira custom-fields
    public static final String DEFAULT_REPLACEMENT = "None";

    private final String providerName;
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;

    public static JiraCustomFieldReplacementValues trivial(LinkableItem provider) {
        return new JiraCustomFieldReplacementValues(provider.getLabel(), DEFAULT_REPLACEMENT, null, null, null);
    }

    public JiraCustomFieldReplacementValues(
        String providerName,
        String projectName,
        @Nullable String projectVersionName,
        @Nullable String componentName,
        @Nullable String componentVersionName
    ) {
        this.providerName = providerName;
        this.projectName = projectName;
        this.projectVersionName = StringUtils.trimToNull(projectVersionName);
        this.componentName = StringUtils.trimToNull(componentName);
        this.componentVersionName = StringUtils.trimToNull(componentVersionName);
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public Optional<String> getProjectVersionName() {
        return Optional.ofNullable(projectVersionName);
    }

    public Optional<String> getComponentName() {
        return Optional.ofNullable(componentName);
    }

    public Optional<String> getComponentVersionName() {
        return Optional.ofNullable(componentVersionName);
    }

}
