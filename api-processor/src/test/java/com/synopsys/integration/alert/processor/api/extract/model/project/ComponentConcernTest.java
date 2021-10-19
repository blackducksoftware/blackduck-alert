package com.synopsys.integration.alert.processor.api.extract.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;

public class ComponentConcernTest {
    private static final String POLICY_NAME = "PolicyName";
    private static final String VULNERABILITY_NAME = "VulnerabilityName";

    @Test
    public void getTypeTest() {
        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern severePolicyConcern = ComponentConcern.severePolicy(ItemOperation.ADD, POLICY_NAME, ComponentConcernSeverity.CRITICAL, "http://policyUrl");
        ComponentConcern vulnerabilityConcern = ComponentConcern.vulnerability(ItemOperation.ADD, VULNERABILITY_NAME, ComponentConcernSeverity.CRITICAL, "https://vulnUr");

        assertEquals(ComponentConcernType.POLICY, policyConcern.getType());
        assertEquals(ComponentConcernType.POLICY, severePolicyConcern.getType());
        assertEquals(ComponentConcernType.VULNERABILITY, vulnerabilityConcern.getType());
    }

    @Test
    public void getOperationTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.DELETE, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern3 = ComponentConcern.policy(ItemOperation.INFO, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern4 = ComponentConcern.policy(ItemOperation.UPDATE, POLICY_NAME, "http://policyUrl");

        assertEquals(ItemOperation.ADD, policyConcern1.getOperation());
        assertEquals(ItemOperation.DELETE, policyConcern2.getOperation());
        assertEquals(ItemOperation.INFO, policyConcern3.getOperation());
        assertEquals(ItemOperation.UPDATE, policyConcern4.getOperation());
    }

    @Test
    public void getNameTest() {
        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");

        assertEquals(POLICY_NAME, policyConcern.getName());
    }

    @Test
    public void getSeverityTest() {
        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern severePolicyConcern = ComponentConcern.severePolicy(ItemOperation.ADD, POLICY_NAME, ComponentConcernSeverity.CRITICAL, "http://policyUrl");
        ComponentConcern vulnerabilityConcern = ComponentConcern.vulnerability(ItemOperation.ADD, VULNERABILITY_NAME, ComponentConcernSeverity.MAJOR_HIGH, "https://vulnUr");

        assertEquals(ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, policyConcern.getSeverity());
        assertEquals(ComponentConcernSeverity.CRITICAL, severePolicyConcern.getSeverity());
        assertEquals(ComponentConcernSeverity.MAJOR_HIGH, vulnerabilityConcern.getSeverity());
    }

    @Test
    public void getUrlTest() {
        String policyUrl = "http://policyUrl";
        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, policyUrl);
        ComponentConcern policyConcernWithoutUrl = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, null);

        assertTrue(policyConcern.getUrl().isPresent());
        assertEquals(policyUrl, policyConcern.getUrl().get());
        assertTrue(policyConcernWithoutUrl.getUrl().isEmpty());
    }

    @Test
    public void combineNameDoesNotMatchTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.ADD, "Different Policy Name", "http://policyUrl");

        List<ComponentConcern> componentConcerns = policyConcern1.combine(policyConcern2);
        assertEquals(2, componentConcerns.size());
        assertTrue(componentConcerns.contains(policyConcern1));
        assertTrue(componentConcerns.contains(policyConcern2));
    }

    @Test
    public void combineTypeDoesNotMatchTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern vulnerabilityConcern1 = ComponentConcern.vulnerability(ItemOperation.ADD, VULNERABILITY_NAME, ComponentConcernSeverity.CRITICAL, "https://vulnUr");

        List<ComponentConcern> componentConcerns = policyConcern1.combine(vulnerabilityConcern1);
        assertEquals(2, componentConcerns.size());
        assertTrue(componentConcerns.contains(policyConcern1));
        assertTrue(componentConcerns.contains(vulnerabilityConcern1));
    }

    @Test
    public void combineOperationMatches() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl2");

        List<ComponentConcern> componentConcerns = policyConcern1.combine(policyConcern2);
        assertEquals(1, componentConcerns.size());
        assertTrue(componentConcerns.contains(policyConcern1));
    }

    @Test
    public void combineLeftAddRightDeleteOperationTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.DELETE, POLICY_NAME, "http://policyUrl2");

        List<ComponentConcern> componentConcerns = policyConcern1.combine(policyConcern2);
        assertTrue(componentConcerns.isEmpty());
    }

    @Test
    public void combineLeftDeleteRightAddOperationTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.DELETE, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl2");

        List<ComponentConcern> componentConcerns = policyConcern1.combine(policyConcern2);
        assertEquals(1, componentConcerns.size());
        assertTrue(componentConcerns.contains(policyConcern2));
    }

    @Test
    public void combineInfoAndAddOperationTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.INFO, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl2");

        List<ComponentConcern> componentConcerns = policyConcern1.combine(policyConcern2);
        assertEquals(2, componentConcerns.size());
        assertTrue(componentConcerns.contains(policyConcern1));
        assertTrue(componentConcerns.contains(policyConcern2));
    }

    @Test
    public void compareToDifferentTypesTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern vulnerabilityConcern1 = ComponentConcern.vulnerability(ItemOperation.ADD, VULNERABILITY_NAME, ComponentConcernSeverity.CRITICAL, "https://vulnUr");

        assertEquals(ComponentConcernType.POLICY.compareTo(ComponentConcernType.VULNERABILITY), policyConcern1.compareTo(vulnerabilityConcern1));
    }

    @Test
    public void compareToDifferentOperationTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.DELETE, POLICY_NAME, "http://policyUrl2");

        assertEquals(-1, policyConcern1.compareTo(policyConcern2));
    }

    @Test
    public void compareToDifferentSeverityTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.severePolicy(ItemOperation.ADD, POLICY_NAME, ComponentConcernSeverity.CRITICAL, "http://policyUrl2");

        //UNSPECIFIED_UNKNOWN is ordinal 5 while CRITICAL is ordinal 1
        assertEquals(4, policyConcern1.compareTo(policyConcern2));
    }

    @Test
    public void compareToDifferentNameTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.ADD, "This policy name does not match", "http://policyUrl2");

        assertTrue(policyConcern1.compareTo(policyConcern2) < 0);
    }

    @Test
    public void compareToTest() {
        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, "http://policyUrl2");

        assertEquals(0, policyConcern1.compareTo(policyConcern2));
    }

}
