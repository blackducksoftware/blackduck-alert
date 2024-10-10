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

public class RuleViolationUniquePolicyNotificationContent extends AbstractRuleViolationNotificationContent {
    private final int componentVersionsInViolation;

    public RuleViolationUniquePolicyNotificationContent(
        String projectName,
        String projectVersionName,
        String projectVersion,
        int componentVersionsInViolation,
        List<ComponentVersionStatus> componentVersionStatuses,
        PolicyInfo policyInfo
    ) {
        super(projectName, projectVersionName, projectVersion, componentVersionStatuses, policyInfo);
        this.componentVersionsInViolation = componentVersionsInViolation;
    }

    public int getComponentVersionsInViolation() {
        return componentVersionsInViolation;
    }

}
