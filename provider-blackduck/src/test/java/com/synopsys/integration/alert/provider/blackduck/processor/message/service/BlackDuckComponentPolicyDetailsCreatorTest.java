package com.synopsys.integration.alert.provider.blackduck.processor.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleCategoryType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentPolicyRulesView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.service.dataservice.PolicyRuleService;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckComponentPolicyDetailsCreatorTest {
    private static final String EXAMPLE_VULNERABILITY_EXPRESSION = "a-vuln-expression";
    public static final BlackDuckPolicySeverityConverter POLICY_SEVERITY_CONVERTER = new BlackDuckPolicySeverityConverter();

    @Test
    public void toComponentPolicyTest() throws IntegrationException {
        String policyName = "alert-test-policy-01";
        PolicyRuleSeverityType severity = PolicyRuleSeverityType.MAJOR;
        PolicyRuleService policyRuleService = Mockito.mock(PolicyRuleService.class);

        BlackDuckComponentPolicyDetailsCreator policyDetailsCreator = new BlackDuckComponentPolicyDetailsCreator(POLICY_SEVERITY_CONVERTER, policyRuleService);

        ComponentPolicyRulesView componentPolicyRulesView = new ComponentPolicyRulesView();
        componentPolicyRulesView.setName(policyName);
        componentPolicyRulesView.setSeverity(severity);
        componentPolicyRulesView.setPolicyApprovalStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION);

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(componentPolicyRulesView.getName());
        policyRuleView.setCategory(PolicyRuleCategoryType.UNCATEGORIZED);
        Mockito.when(policyRuleService.getPolicyRuleViewByName(Mockito.anyString())).thenReturn(Optional.of(policyRuleView));

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
        PolicyRuleService policyRuleService = Mockito.mock(PolicyRuleService.class);

        PolicyRuleExpressionView policyRuleExpression = new PolicyRuleExpressionView();
        policyRuleExpression.setExpressions(List.of(expression));

        BlackDuckComponentPolicyDetailsCreator policyDetailsCreator = new BlackDuckComponentPolicyDetailsCreator(POLICY_SEVERITY_CONVERTER, policyRuleService);

        ComponentPolicyRulesView componentPolicyRulesView = new ComponentPolicyRulesView();
        componentPolicyRulesView.setName("vuln-test-policy");
        componentPolicyRulesView.setSeverity(PolicyRuleSeverityType.TRIVIAL);
        componentPolicyRulesView.setPolicyApprovalStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION);
        componentPolicyRulesView.setExpression(policyRuleExpression);

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(componentPolicyRulesView.getName());
        policyRuleView.setCategory(PolicyRuleCategoryType.UNCATEGORIZED);
        Mockito.when(policyRuleService.getPolicyRuleViewByName(Mockito.anyString())).thenReturn(Optional.of(policyRuleView));

        ComponentPolicy componentPolicy = policyDetailsCreator.toComponentPolicy(componentPolicyRulesView);
        assertTrue(componentPolicy.isVulnerabilityPolicy(), "Expected a vulnerability policy");
    }

    @Test
    public void toComponentPolicyOverriddenTest() throws IntegrationException {
        PolicyRuleExpressionExpressionsView expression = new PolicyRuleExpressionExpressionsView();
        expression.setName(EXAMPLE_VULNERABILITY_EXPRESSION);
        PolicyRuleService policyRuleService = Mockito.mock(PolicyRuleService.class);

        PolicyRuleExpressionView policyRuleExpression = new PolicyRuleExpressionView();
        policyRuleExpression.setExpressions(List.of(expression));

        BlackDuckComponentPolicyDetailsCreator policyDetailsCreator = new BlackDuckComponentPolicyDetailsCreator(POLICY_SEVERITY_CONVERTER, policyRuleService);

        ComponentPolicyRulesView componentPolicyRulesView = new ComponentPolicyRulesView();
        componentPolicyRulesView.setName("override-test-policy");
        componentPolicyRulesView.setSeverity(PolicyRuleSeverityType.TRIVIAL);
        componentPolicyRulesView.setPolicyApprovalStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION_OVERRIDDEN);

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(componentPolicyRulesView.getName());
        policyRuleView.setCategory(PolicyRuleCategoryType.UNCATEGORIZED);
        Mockito.when(policyRuleService.getPolicyRuleViewByName(Mockito.anyString())).thenReturn(Optional.of(policyRuleView));

        ComponentPolicy componentPolicy = policyDetailsCreator.toComponentPolicy(componentPolicyRulesView);
        assertTrue(componentPolicy.isOverridden(), "Expected the policy to be overridden");
    }

}
