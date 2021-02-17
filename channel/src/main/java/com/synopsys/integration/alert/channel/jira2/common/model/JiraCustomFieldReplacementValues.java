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

public class JiraCustomFieldReplacementValues {
    private final String providerName;
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;

    public JiraCustomFieldReplacementValues(String providerName, String projectName, String projectVersionName, String componentName, String componentVersionName) {
        this.providerName = providerName;
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.componentName = componentName;
        this.componentVersionName = componentVersionName;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

}
