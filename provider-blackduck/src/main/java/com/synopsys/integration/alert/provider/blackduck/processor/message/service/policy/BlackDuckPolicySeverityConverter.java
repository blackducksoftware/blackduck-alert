/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy;

import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;

@Component
public class BlackDuckPolicySeverityConverter {
    private static final Map<PolicyRuleSeverityType, ComponentConcernSeverity> SEVERITY_MAP = Map.of(
        PolicyRuleSeverityType.BLOCKER, ComponentConcernSeverity.BLOCKER,
        PolicyRuleSeverityType.CRITICAL, ComponentConcernSeverity.CRITICAL,
        PolicyRuleSeverityType.MAJOR, ComponentConcernSeverity.MAJOR_HIGH,
        PolicyRuleSeverityType.MINOR, ComponentConcernSeverity.MINOR_MEDIUM,
        PolicyRuleSeverityType.TRIVIAL, ComponentConcernSeverity.TRIVIAL_LOW,
        PolicyRuleSeverityType.UNSPECIFIED, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN
    );

    public ComponentConcernSeverity toComponentConcernSeverity(String policySeverity) {
        PolicyRuleSeverityType policyRuleSeverityType = EnumUtils.getEnum(PolicyRuleSeverityType.class, policySeverity, PolicyRuleSeverityType.UNSPECIFIED);
        return SEVERITY_MAP.getOrDefault(policyRuleSeverityType, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
    }

}
