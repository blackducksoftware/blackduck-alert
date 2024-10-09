package com.blackduck.integration.alert.provider.blackduck.processor.model;

import java.util.List;

import com.blackduck.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.blackduck.integration.blackduck.api.manual.component.PolicyInfo;

public abstract class AbstractRuleViolationNotificationContent extends AbstractProjectVersionNotificationContent {
    private final List<ComponentVersionStatus> componentVersionStatuses;
    private final PolicyInfo policyInfo;

    public AbstractRuleViolationNotificationContent(
        String projectName,
        String projectVersionName,
        String projectVersion,
        List<ComponentVersionStatus> componentVersionStatuses,
        PolicyInfo policyInfo
    ) {
        super(projectName, projectVersionName, projectVersion);
        this.componentVersionStatuses = componentVersionStatuses;
        this.policyInfo = policyInfo;
    }

    public List<ComponentVersionStatus> getComponentVersionStatuses() {
        return componentVersionStatuses;
    }

    public PolicyInfo getPolicyInfo() {
        return policyInfo;
    }

}
