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
package com.synopsys.integration.alert.processor.api.filter.extractor;

import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public class PolicyOverrideUniquePolicyNotificationContent extends NotificationContentComponent {
    private final String projectName;
    private final String projectVersionName;
    private final String projectVersion;
    private final String componentName;
    private final String componentVersionName;
    private final String firstName;
    private final String lastName;
    private final PolicyInfo policyInfo;
    private final String policy;
    private final String bomComponentVersionPolicyStatus;
    private final String bomComponent;

    public PolicyOverrideUniquePolicyNotificationContent(
        String projectName,
        String projectVersionName,
        String projectVersion,
        String componentName,
        String componentVersionName,
        String firstName,
        String lastName,
        PolicyInfo policyInfo,
        String policy,
        String bomComponentVersionPolicyStatus,
        String bomComponent
    ) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersion = projectVersion;
        this.componentName = componentName;
        this.componentVersionName = componentVersionName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.policyInfo = policyInfo;
        this.policy = policy;
        this.bomComponentVersionPolicyStatus = bomComponentVersionPolicyStatus;
        this.bomComponent = bomComponent;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public PolicyInfo getPolicyInfo() {
        return policyInfo;
    }

    public String getPolicy() {
        return policy;
    }

    public String getBomComponentVersionPolicyStatus() {
        return bomComponentVersionPolicyStatus;
    }

    public String getBomComponent() {
        return bomComponent;
    }

}
