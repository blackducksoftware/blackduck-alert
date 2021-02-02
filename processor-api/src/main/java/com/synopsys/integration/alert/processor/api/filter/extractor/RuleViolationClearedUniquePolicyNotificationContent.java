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

import java.util.List;

import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public class RuleViolationClearedUniquePolicyNotificationContent extends NotificationContentComponent {
    private final String projectName;
    private final String projectVersionName;
    private final String projectVersion;
    private final int componentVersionsCleared;
    private final List<ComponentVersionStatus> componentVersionStatuses;
    private final PolicyInfo policyInfo;

    public RuleViolationClearedUniquePolicyNotificationContent(String projectName, String projectVersionName, String projectVersion, int componentVersionsCleared,
        List<ComponentVersionStatus> componentVersionStatuses, PolicyInfo policyInfo) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersion = projectVersion;
        this.componentVersionsCleared = componentVersionsCleared;
        this.componentVersionStatuses = componentVersionStatuses;
        this.policyInfo = policyInfo;
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

    public int getComponentVersionsCleared() {
        return componentVersionsCleared;
    }

    public List<ComponentVersionStatus> getComponentVersionStatuses() {
        return componentVersionStatuses;
    }

    public PolicyInfo getPolicyInfo() {
        return policyInfo;
    }

}
