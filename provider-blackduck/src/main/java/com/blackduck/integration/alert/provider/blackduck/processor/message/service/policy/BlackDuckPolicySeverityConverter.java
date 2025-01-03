/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy;

import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;

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
