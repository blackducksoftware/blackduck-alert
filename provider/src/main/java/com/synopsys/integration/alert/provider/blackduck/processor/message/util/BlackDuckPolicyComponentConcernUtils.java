/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.util;

import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public final class BlackDuckPolicyComponentConcernUtils {
    public static final Map<PolicyRuleSeverityType, ComponentConcernSeverity> SEVERITY_MAP = Map.of(
        PolicyRuleSeverityType.BLOCKER, ComponentConcernSeverity.BLOCKER,
        PolicyRuleSeverityType.CRITICAL, ComponentConcernSeverity.CRITICAL,
        PolicyRuleSeverityType.MAJOR, ComponentConcernSeverity.MAJOR_HIGH,
        PolicyRuleSeverityType.MINOR, ComponentConcernSeverity.MINOR_MEDIUM,
        PolicyRuleSeverityType.TRIVIAL, ComponentConcernSeverity.TRIVIAL_LOW,
        PolicyRuleSeverityType.UNSPECIFIED, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN
    );

    public static ComponentConcern fromPolicyInfo(PolicyInfo policyInfo, ItemOperation itemOperation) {
        String policyName = policyInfo.getPolicyName();

        String policySeverity = policyInfo.getSeverity();
        if (StringUtils.isNotBlank(policySeverity)) {
            PolicyRuleSeverityType policyRuleSeverityType = EnumUtils.getEnum(PolicyRuleSeverityType.class, policySeverity, PolicyRuleSeverityType.UNSPECIFIED);
            ComponentConcernSeverity componentConcernSeverity = SEVERITY_MAP.getOrDefault(policyRuleSeverityType, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
            return ComponentConcern.severePolicy(itemOperation, policyName, componentConcernSeverity);
        }
        return ComponentConcern.policy(itemOperation, policyName);
    }

    private BlackDuckPolicyComponentConcernUtils() {
    }

}
