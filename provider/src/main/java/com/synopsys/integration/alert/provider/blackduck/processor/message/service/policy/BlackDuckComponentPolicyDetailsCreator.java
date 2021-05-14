/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentPolicyRulesView;

@Component
public class BlackDuckComponentPolicyDetailsCreator {
    private static final String VULNERABILITY_POLICY_EXPRESSION_NAME = "vuln";

    private final BlackDuckPolicySeverityConverter policySeverityConverter;

    @Autowired
    public BlackDuckComponentPolicyDetailsCreator(BlackDuckPolicySeverityConverter policySeverityConverter) {
        this.policySeverityConverter = policySeverityConverter;
    }

    public ComponentPolicy toComponentPolicy(ComponentPolicyRulesView componentPolicyRulesView) {
        ComponentConcernSeverity componentConcernSeverity = policySeverityConverter.toComponentConcernSeverity(componentPolicyRulesView.getSeverity().name());
        boolean overridden = ProjectVersionComponentPolicyStatusType.IN_VIOLATION_OVERRIDDEN.equals(componentPolicyRulesView.getPolicyApprovalStatus());
        boolean vulnerabilityPolicy = isVulnerabilityPolicy(componentPolicyRulesView);
        return new ComponentPolicy(componentPolicyRulesView.getName(), componentConcernSeverity, overridden, vulnerabilityPolicy);
    }

    private boolean isVulnerabilityPolicy(ComponentPolicyRulesView componentPolicyRulesView) {
        PolicyRuleExpressionView policyRuleExpression = componentPolicyRulesView.getExpression();
        if (null != policyRuleExpression) {
            List<PolicyRuleExpressionExpressionsView> policyRuleExpressions = policyRuleExpression.getExpressions();
            if (null != policyRuleExpressions) {
                return policyRuleExpressions
                           .stream()
                           .anyMatch(this::isVulnerabilityExpression);
            }
        }
        return false;
    }

    private boolean isVulnerabilityExpression(PolicyRuleExpressionExpressionsView expression) {
        return null != expression && expression.getName().toLowerCase().contains(VULNERABILITY_POLICY_EXPRESSION_NAME);
    }

}
