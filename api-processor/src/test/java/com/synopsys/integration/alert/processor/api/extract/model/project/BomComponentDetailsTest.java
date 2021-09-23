package com.synopsys.integration.alert.processor.api.extract.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class BomComponentDetailsTest {
    private static final LinkableItem CRITICAL_VULNERABILITY = new LinkableItem("Vulnerability", "CVE-123", "https://vuln-url");

    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final ComponentPolicy COMPONENT_POLICY = new ComponentPolicy("A policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, false, false, null, "Uncategorized");
    private static final ComponentVulnerabilities COMPONENT_VULNERABILITIES = new ComponentVulnerabilities(List.of(CRITICAL_VULNERABILITY), List.of(), List.of(), List.of());
    private static final LinkableItem LICENSE = new LinkableItem("License", "A software license", "https://license-url");
    private static final String USAGE = "Some generic usage";
    private static final String ISSUES_URL = "https://issues-url";

    @Test
    public void getComponentConcernsTest() {
        ComponentConcern componentConcern = ComponentConcern.policy(ItemOperation.ADD, "Added Policy", "https://policy");
        BomComponentDetails bomComponentDetails = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(componentConcern), LICENSE, USAGE, ComponentUpgradeGuidance.none(), List.of(),
            ISSUES_URL);

        assertTrue(bomComponentDetails.hasComponentConcerns());
        assertEquals(1, bomComponentDetails.getComponentConcerns().size());
        assertTrue(bomComponentDetails.getComponentConcerns().contains(componentConcern));
    }

    @Test
    public void combineTest() {
        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, "Added Policy", "https://policy");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.ADD, "Added Policy 2", "https://policy2");

        BomComponentDetails bomComponentDetailsPolicy = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(COMPONENT_POLICY), List.of(policyConcern), LICENSE, USAGE,
            ComponentUpgradeGuidance.none(), List.of(),
            ISSUES_URL);
        BomComponentDetails bomComponentDetailsPolicy2 = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(policyConcern2), LICENSE, USAGE, ComponentUpgradeGuidance.none(),
            List.of(),
            ISSUES_URL);

        List<BomComponentDetails> bomComponentDetailsCombined = bomComponentDetailsPolicy.combine(bomComponentDetailsPolicy2);
        assertEquals(1, bomComponentDetailsCombined.size());
        BomComponentDetails bomComponentDetails = bomComponentDetailsCombined.get(0);
        assertEquals(2, bomComponentDetails.getComponentConcerns().size());
        assertTrue(bomComponentDetails.getComponentConcerns().contains(policyConcern));
        assertTrue(bomComponentDetails.getComponentConcerns().contains(policyConcern2));
        assertEquals(1, bomComponentDetails.getRelevantPolicies().size());
        assertTrue(bomComponentDetails.getRelevantPolicies().contains(COMPONENT_POLICY));
    }

    @Test
    public void combineAdditionalAttributesLeftTest() {
        LinkableItem additionalAttribute1 = new LinkableItem("Attribute 1", "additionalAttribute1");
        LinkableItem additionalAttribute2 = new LinkableItem("Attribute 2", "additionalAttribute2");

        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, "Added Policy", "https://policy");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.ADD, "Added Policy 2", "https://policy2");

        BomComponentDetails bomComponentDetailsPolicy = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(COMPONENT_POLICY), List.of(policyConcern, policyConcern2), LICENSE, USAGE,
            ComponentUpgradeGuidance.none(), List.of(additionalAttribute1),
            ISSUES_URL);
        BomComponentDetails bomComponentDetailsPolicy2 = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(policyConcern2), LICENSE, USAGE, ComponentUpgradeGuidance.none(),
            List.of(additionalAttribute2),
            ISSUES_URL);

        List<BomComponentDetails> bomComponentDetailsCombined = bomComponentDetailsPolicy.combine(bomComponentDetailsPolicy2);
        assertEquals(1, bomComponentDetailsCombined.size());
        BomComponentDetails bomComponentDetails = bomComponentDetailsCombined.get(0);
        assertEquals(1, bomComponentDetails.getAdditionalAttributes().size());
        assertTrue(bomComponentDetails.getAdditionalAttributes().contains(additionalAttribute1));
    }

    @Test
    public void combineAdditionalAttributesRightTest() {
        LinkableItem additionalAttribute1 = new LinkableItem("Attribute 1", "additionalAttribute1");
        LinkableItem additionalAttribute2 = new LinkableItem("Attribute 2", "additionalAttribute2");

        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, "Added Policy", "https://policy");
        ComponentConcern policyConcern2 = ComponentConcern.policy(ItemOperation.ADD, "Added Policy 2", "https://policy2");

        BomComponentDetails bomComponentDetailsPolicy = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(COMPONENT_POLICY), List.of(policyConcern), LICENSE, USAGE,
            ComponentUpgradeGuidance.none(), List.of(additionalAttribute1),
            ISSUES_URL);
        BomComponentDetails bomComponentDetailsPolicy2 = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(policyConcern, policyConcern2), LICENSE, USAGE,
            ComponentUpgradeGuidance.none(),
            List.of(additionalAttribute2),
            ISSUES_URL);

        List<BomComponentDetails> bomComponentDetailsCombined = bomComponentDetailsPolicy.combine(bomComponentDetailsPolicy2);
        assertEquals(1, bomComponentDetailsCombined.size());
        BomComponentDetails bomComponentDetails = bomComponentDetailsCombined.get(0);
        assertEquals(1, bomComponentDetails.getAdditionalAttributes().size());
        assertTrue(bomComponentDetails.getAdditionalAttributes().contains(additionalAttribute2));
    }

    @Test
    public void combineWithDifferentComponentsTest() {
        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, "Added Policy", "https://policy");
        ComponentConcern vulnerabilityConcern = ComponentConcern.vulnerability(ItemOperation.ADD, "Added-Vuln01", ComponentConcernSeverity.CRITICAL, "https://synopsys.com");
        LinkableItem differentComponent = new LinkableItem("Component-does-not-match", "A BOM component");

        BomComponentDetails bomComponentDetailsPolicy = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(policyConcern), LICENSE, USAGE, ComponentUpgradeGuidance.none(), List.of(),
            ISSUES_URL);
        BomComponentDetails bomComponentDetailsVulnerability = new BomComponentDetails(differentComponent, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(vulnerabilityConcern), LICENSE, USAGE,
            ComponentUpgradeGuidance.none(),
            List.of(),
            ISSUES_URL);

        List<BomComponentDetails> bomComponentDetailsList = bomComponentDetailsPolicy.combine(bomComponentDetailsVulnerability);
        assertEquals(2, bomComponentDetailsList.size());
        assertTrue(bomComponentDetailsList.contains(bomComponentDetailsPolicy));
        assertTrue(bomComponentDetailsList.contains(bomComponentDetailsVulnerability));
    }

    @Test
    public void combineOneComponentVersionNullTest() {
        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, "Added Policy", "https://policy");
        ComponentConcern vulnerabilityConcern = ComponentConcern.vulnerability(ItemOperation.ADD, "Added-Vuln01", ComponentConcernSeverity.CRITICAL, "https://synopsys.com");

        BomComponentDetails bomComponentDetailsPolicy = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(policyConcern), LICENSE, USAGE, ComponentUpgradeGuidance.none(), List.of(),
            ISSUES_URL);
        BomComponentDetails bomComponentDetailsVulnerability = new BomComponentDetails(COMPONENT, null, ComponentVulnerabilities.none(), List.of(), List.of(vulnerabilityConcern), LICENSE, USAGE,
            ComponentUpgradeGuidance.none(),
            List.of(),
            ISSUES_URL);

        List<BomComponentDetails> bomComponentDetailsList = bomComponentDetailsPolicy.combine(bomComponentDetailsVulnerability);
        assertEquals(2, bomComponentDetailsList.size());
        assertTrue(bomComponentDetailsList.contains(bomComponentDetailsPolicy));
        assertTrue(bomComponentDetailsList.contains(bomComponentDetailsVulnerability));
    }

    @Test
    public void combineOneComponentMissingComponentConcernsTest() {
        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, "Added Policy", "https://policy");

        BomComponentDetails bomComponentDetailsPolicy = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(policyConcern), LICENSE, USAGE, ComponentUpgradeGuidance.none(), List.of(),
            ISSUES_URL);
        BomComponentDetails bomComponentDetailsVulnerability = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(), LICENSE, USAGE, ComponentUpgradeGuidance.none(),
            List.of(),
            ISSUES_URL);

        List<BomComponentDetails> bomComponentDetailsList = bomComponentDetailsPolicy.combine(bomComponentDetailsVulnerability);
        assertEquals(2, bomComponentDetailsList.size());
        assertTrue(bomComponentDetailsList.contains(bomComponentDetailsPolicy));
        assertTrue(bomComponentDetailsList.contains(bomComponentDetailsVulnerability));
    }
}
