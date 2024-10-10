/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.model;

import java.util.List;

import com.blackduck.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.blackduck.integration.blackduck.api.manual.component.PolicyInfo;

public class RuleViolationClearedUniquePolicyNotificationContent extends AbstractRuleViolationNotificationContent {
    private final int componentVersionsCleared;

    public RuleViolationClearedUniquePolicyNotificationContent(String projectName, String projectVersionName, String projectVersion, int componentVersionsCleared,
        List<ComponentVersionStatus> componentVersionStatuses, PolicyInfo policyInfo) {
        super(projectName, projectVersionName, projectVersion, componentVersionStatuses, policyInfo);
        this.componentVersionsCleared = componentVersionsCleared;
    }

    public int getComponentVersionsCleared() {
        return componentVersionsCleared;
    }

}
