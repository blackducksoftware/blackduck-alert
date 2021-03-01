/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.util;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public final class BlackDuckMessageComponentConcernUtils {
    public static ComponentConcern fromPolicyInfo(PolicyInfo policyInfo, ItemOperation itemOperation) {
        String policyName = policyInfo.getPolicyName();

        String policySeverity = policyInfo.getSeverity();
        if (StringUtils.isNotBlank(policySeverity)) {
            ComponentConcernSeverity severity = EnumUtils.getEnum(ComponentConcernSeverity.class, policySeverity);
            return ComponentConcern.severePolicy(itemOperation, policyName, severity);
        }
        return ComponentConcern.policy(itemOperation, policyName);
    }

    private BlackDuckMessageComponentConcernUtils() {
    }

}
