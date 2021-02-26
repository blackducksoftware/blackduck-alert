/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.collector.builder.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;

public final class PolicyPriorityUtil {
    private static Map<String, ComponentItemPriority> policyPriorityMap = Map.of(
        "blocker", ComponentItemPriority.HIGHEST,
        "critical", ComponentItemPriority.HIGH,
        "major", ComponentItemPriority.MEDIUM,
        "minor", ComponentItemPriority.LOW,
        "trivial", ComponentItemPriority.LOWEST,
        "unspecified", ComponentItemPriority.NONE);

    private PolicyPriorityUtil() {
    }

    public static ComponentItemPriority getPriorityFromSeverity(String severity) {
        if (StringUtils.isNotBlank(severity)) {
            String severityKey = severity.trim().toLowerCase();
            return policyPriorityMap.getOrDefault(severityKey, ComponentItemPriority.NONE);
        }
        return ComponentItemPriority.NONE;
    }

    public static ComponentItemPriority getPriorityFromSeverity(PolicyRuleSeverityType severity) {
        if (null != severity) {
            String severityKey = severity.name().toLowerCase();
            return policyPriorityMap.getOrDefault(severityKey, ComponentItemPriority.NONE);
        }
        return ComponentItemPriority.NONE;
    }
}
