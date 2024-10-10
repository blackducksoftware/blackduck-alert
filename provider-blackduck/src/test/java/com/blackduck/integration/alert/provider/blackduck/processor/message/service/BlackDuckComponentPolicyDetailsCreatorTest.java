/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentPolicy;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.blackduck.integration.blackduck.api.core.ResourceMetadata;
import com.blackduck.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.blackduck.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.blackduck.integration.blackduck.api.generated.enumeration.PolicyRuleCategoryType;
import com.blackduck.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.blackduck.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.blackduck.integration.blackduck.api.generated.view.ComponentPolicyRulesView;
import com.blackduck.integration.blackduck.api.generated.view.PolicyRuleView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;

public class BlackDuckComponentPolicyDetailsCreatorTest {
    private static final String EXAMPLE_VULNERABILITY_EXPRESSION = "a-vuln-expression";
    public static final BlackDuckPolicySeverityConverter POLICY_SEVERITY_CONVERTER = new BlackDuckPolicySeverityConverter();

    @Test
    public void toComponentPolicyTest() throws IntegrationException {
        String policyName = "alert-test-policy-01";
        PolicyRuleSeverityType severity = PolicyRuleSeverityType.MAJOR;
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);

        BlackDuckComponentPolicyDetailsCreator policyDetailsCreator = new BlackDuckComponentPolicyDetailsCreator(POLICY_SEVERITY_CONVERTER, blackDuckApiClient);

        ComponentPolicyRulesView componentPolicyRulesView = new ComponentPolicyRulesView();
        componentPolicyRulesView.setName(policyName);
        componentPolicyRulesView.setSeverity(severity);
        componentPolicyRulesView.setPolicyApprovalStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION);
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("https://someUrl"));
        componentPolicyRulesView.setMeta(meta);

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(componentPolicyRulesView.getName());
        policyRuleView.setCategory(PolicyRuleCategoryType.UNCATEGORIZED);
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.any())).thenReturn(policyRuleView);

        ComponentPolicy componentPolicy = policyDetailsCreator.toComponentPolicy(componentPolicyRulesView);

        assertEquals(policyName, componentPolicy.getPolicyName());
        assertEquals(severity.name(), componentPolicy.getSeverity().getPolicyLabel());
        assertFalse(componentPolicy.isVulnerabilityPolicy(), "Did not expect a vulnerability policy");
        assertFalse(componentPolicy.isOverridden(), "Did not expect the policy to be overridden");
        assertTrue(componentPolicy.getCategory().isPresent());
        assertEquals(PolicyRuleCategoryType.UNCATEGORIZED.name(), componentPolicy.getCategory().get());
    }

    @Test
    public void toComponentPolicyVulnerabilityRuleTest() throws IntegrationException {
        PolicyRuleExpressionExpressionsView expression = new PolicyRuleExpressionExpressionsView();
        expression.setName(EXAMPLE_VULNERABILITY_EXPRESSION);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);

        PolicyRuleExpressionView policyRuleExpression = new PolicyRuleExpressionView();
        policyRuleExpression.setExpressions(List.of(expression));

        BlackDuckComponentPolicyDetailsCreator policyDetailsCreator = new BlackDuckComponentPolicyDetailsCreator(POLICY_SEVERITY_CONVERTER, blackDuckApiClient);

        ComponentPolicyRulesView componentPolicyRulesView = new ComponentPolicyRulesView();
        componentPolicyRulesView.setName("vuln-test-policy");
        componentPolicyRulesView.setSeverity(PolicyRuleSeverityType.TRIVIAL);
        componentPolicyRulesView.setPolicyApprovalStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION);
        componentPolicyRulesView.setExpression(policyRuleExpression);
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("https://someUrl"));
        componentPolicyRulesView.setMeta(meta);

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(componentPolicyRulesView.getName());
        policyRuleView.setCategory(PolicyRuleCategoryType.UNCATEGORIZED);
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.any())).thenReturn(policyRuleView);

        ComponentPolicy componentPolicy = policyDetailsCreator.toComponentPolicy(componentPolicyRulesView);
        assertTrue(componentPolicy.isVulnerabilityPolicy(), "Expected a vulnerability policy");
    }

    @Test
    public void toComponentPolicyOverriddenTest() throws IntegrationException {
        PolicyRuleExpressionExpressionsView expression = new PolicyRuleExpressionExpressionsView();
        expression.setName(EXAMPLE_VULNERABILITY_EXPRESSION);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);

        PolicyRuleExpressionView policyRuleExpression = new PolicyRuleExpressionView();
        policyRuleExpression.setExpressions(List.of(expression));

        BlackDuckComponentPolicyDetailsCreator policyDetailsCreator = new BlackDuckComponentPolicyDetailsCreator(POLICY_SEVERITY_CONVERTER, blackDuckApiClient);

        ComponentPolicyRulesView componentPolicyRulesView = new ComponentPolicyRulesView();
        componentPolicyRulesView.setName("override-test-policy");
        componentPolicyRulesView.setSeverity(PolicyRuleSeverityType.TRIVIAL);
        componentPolicyRulesView.setPolicyApprovalStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION_OVERRIDDEN);
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("https://someUrl"));
        componentPolicyRulesView.setMeta(meta);

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(componentPolicyRulesView.getName());
        policyRuleView.setCategory(PolicyRuleCategoryType.UNCATEGORIZED);
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.any())).thenReturn(policyRuleView);

        ComponentPolicy componentPolicy = policyDetailsCreator.toComponentPolicy(componentPolicyRulesView);
        assertTrue(componentPolicy.isOverridden(), "Expected the policy to be overridden");
    }

}
