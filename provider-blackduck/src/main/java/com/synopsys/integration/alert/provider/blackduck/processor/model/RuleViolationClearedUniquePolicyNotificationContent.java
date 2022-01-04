/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.model;

import java.util.List;

import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

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
