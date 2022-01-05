/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

@Component
public class BlackDuckPolicyComponentConcernCreator {
    private final BlackDuckPolicySeverityConverter policySeverityConverter;

    @Autowired
    public BlackDuckPolicyComponentConcernCreator(BlackDuckPolicySeverityConverter policySeverityConverter) {
        this.policySeverityConverter = policySeverityConverter;
    }

    public ComponentConcern fromPolicyInfo(PolicyInfo policyInfo, ItemOperation itemOperation) {
        String policyName = policyInfo.getPolicyName();
        String policyUrl = policyInfo.getPolicy();

        String policySeverity = policyInfo.getSeverity();
        if (StringUtils.isNotBlank(policySeverity)) {
            ComponentConcernSeverity componentConcernSeverity = policySeverityConverter.toComponentConcernSeverity(policySeverity);
            return ComponentConcern.severePolicy(itemOperation, policyName, componentConcernSeverity, policyUrl);
        }
        return ComponentConcern.policy(itemOperation, policyName, policyUrl);
    }

}
