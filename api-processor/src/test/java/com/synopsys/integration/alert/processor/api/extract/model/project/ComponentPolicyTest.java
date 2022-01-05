package com.synopsys.integration.alert.processor.api.extract.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ComponentPolicyTest {
    private static final String POLICY_NAME = "ComponentPolicyName";
    private static final String DESCRIPTION = "A Policy Description";
    private static final String CATEGORY = "Uncategorized";

    @Test
    public void combineTest() {
        ComponentPolicy componentPolicy = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.CRITICAL, false, false, DESCRIPTION, CATEGORY);
        ComponentPolicy componentPolicy2 = new ComponentPolicy("PolicyName2", ComponentConcernSeverity.TRIVIAL_LOW, false, false, "description2", "category2");

        List<ComponentPolicy> policies = componentPolicy.combine(componentPolicy2);
        assertEquals(2, policies.size());
        assertTrue(policies.contains(componentPolicy));
        assertTrue(policies.contains(componentPolicy2));
    }

    @Test
    public void combineSameNameTest() {
        ComponentPolicy componentPolicy = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.CRITICAL, false, false, DESCRIPTION, CATEGORY);
        ComponentPolicy componentPolicy2 = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.TRIVIAL_LOW, false, false, "description2", "category2");

        List<ComponentPolicy> policies = componentPolicy.combine(componentPolicy2);
        assertEquals(1, policies.size());
        assertTrue(policies.contains(componentPolicy));
    }

    @Test
    public void getPolicyNameTest() {
        ComponentPolicy componentPolicy = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.CRITICAL, false, false, DESCRIPTION, CATEGORY);
        assertEquals(POLICY_NAME, componentPolicy.getPolicyName());
    }

    @Test
    public void getSeverityTest() {
        ComponentConcernSeverity severity = ComponentConcernSeverity.BLOCKER;
        ComponentPolicy componentPolicy = new ComponentPolicy(POLICY_NAME, severity, false, false, DESCRIPTION, CATEGORY);
        assertEquals(severity, componentPolicy.getSeverity());
    }

    @Test
    public void isOverwrittenTest() {
        ComponentPolicy componentPolicy = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.CRITICAL, true, false, DESCRIPTION, CATEGORY);
        assertTrue(componentPolicy.isOverridden());
    }

    @Test
    public void isVulnerabilityPolicyTest() {
        ComponentPolicy componentPolicy = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.CRITICAL, true, true, DESCRIPTION, CATEGORY);
        assertTrue(componentPolicy.isVulnerabilityPolicy());
    }

    @Test
    public void getDescriptionTest() {
        ComponentPolicy componentPolicy = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.CRITICAL, false, false, DESCRIPTION, CATEGORY);
        assertTrue(componentPolicy.getDescription().isPresent());
        assertEquals(DESCRIPTION, componentPolicy.getDescription().get());

        ComponentPolicy componentPolicyNullDescription = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.CRITICAL, false, false, null, CATEGORY);
        assertTrue(componentPolicyNullDescription.getDescription().isEmpty());
    }

    @Test
    public void getCategoryTest() {
        ComponentPolicy componentPolicy = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.CRITICAL, false, false, DESCRIPTION, CATEGORY);
        assertTrue(componentPolicy.getCategory().isPresent());
        assertEquals(CATEGORY, componentPolicy.getCategory().get());

        ComponentPolicy componentPolicyNullCategory = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.CRITICAL, false, false, DESCRIPTION, null);
        assertTrue(componentPolicyNullCategory.getCategory().isEmpty());
    }
}
