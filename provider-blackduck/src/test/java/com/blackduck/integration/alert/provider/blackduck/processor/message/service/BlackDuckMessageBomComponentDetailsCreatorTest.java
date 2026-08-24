/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentPolicy;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreator;
import com.blackduck.integration.blackduck.api.core.ResourceLink;
import com.blackduck.integration.blackduck.api.core.ResourceMetadata;
import com.blackduck.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.blackduck.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.blackduck.integration.blackduck.api.generated.component.RiskProfileCountsView;
import com.blackduck.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.blackduck.integration.blackduck.api.generated.enumeration.RiskPriorityType;
import com.blackduck.integration.blackduck.api.generated.enumeration.UsageType;
import com.blackduck.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.blackduck.integration.blackduck.api.generated.view.ComponentPolicyRulesView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.RiskProfileView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;

class BlackDuckMessageBomComponentDetailsCreatorTest {
    private static final String COMPONENT_NAME = "test-component";
    private static final String COMPONENT_VERSION_NAME = "1.2.3";
    private static final String COMPONENT_VERSION_URL = "https://hub/api/components/abc/versions/def";
    private static final String BOM_COMPONENT_HREF =
        "https://hub/api/projects/00000000-0000-0000-0000-000000000001"
            + "/versions/00000000-0000-0000-0000-000000000002"
            + "/components/00000000-0000-0000-0000-000000000003"
            + "/versions/00000000-0000-0000-0000-000000000004";
    private static final String EXPECTED_VULN_ENDPOINT =
        "https://hub/api/projects/00000000-0000-0000-0000-000000000001"
            + "/versions/00000000-0000-0000-0000-000000000002"
            + "/vulnerabilities";
    private static final String POLICY_NAME = "test-policy";

    private BlackDuckApiClient blackDuckApiClient;
    private BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator;
    private BlackDuckComponentPolicyDetailsCreator policyDetailsCreator;
    private BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator;

    @BeforeEach
    void setUp() {
        blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        vulnerabilityDetailsCreator = Mockito.mock(BlackDuckComponentVulnerabilityDetailsCreator.class);
        policyDetailsCreator = Mockito.mock(BlackDuckComponentPolicyDetailsCreator.class);
        bomComponentDetailsCreator = new BlackDuckMessageBomComponentDetailsCreator(blackDuckApiClient, vulnerabilityDetailsCreator, policyDetailsCreator);
    }

    @Test
    void retrieveVulnerabilitiesNoSecurityRiskTest() throws IntegrationException {
        Mockito.when(vulnerabilityDetailsCreator.hasSecurityRisk(Mockito.any())).thenReturn(false);

        ProjectVersionComponentVersionView bomComponent = createBomComponent(BOM_COMPONENT_HREF, COMPONENT_VERSION_URL, false);
        BomComponentDetails bomComponentDetails = bomComponentDetailsCreator.createBomComponentDetails(bomComponent, List.of(), ComponentUpgradeGuidance.none(), List.of());

        assertFalse(bomComponentDetails.getComponentVulnerabilities().hasVulnerabilities(), "No vulnerabilities expected when no security risk");
        Mockito.verify(blackDuckApiClient, Mockito.never()).getAllResponses(Mockito.<BlackDuckMultipleRequest<BlackDuckVersionBomVulnerabilityView>>any());
    }

    @Test
    void retrieveVulnerabilitiesBlankVersionUrlTest() throws IntegrationException {
        Mockito.when(vulnerabilityDetailsCreator.hasSecurityRisk(Mockito.any())).thenReturn(true);
        Mockito.when(vulnerabilityDetailsCreator.toComponentVulnerabilities(Mockito.any())).thenReturn(ComponentVulnerabilities.none());

        ProjectVersionComponentVersionView bomComponent = createBomComponent(BOM_COMPONENT_HREF, "", false);
        BomComponentDetails bomComponentDetails = bomComponentDetailsCreator.createBomComponentDetails(bomComponent, List.of(), ComponentUpgradeGuidance.none(), List.of());

        assertFalse(bomComponentDetails.getComponentVulnerabilities().hasVulnerabilities(), "No vulnerabilities expected when component version URL is blank");
        Mockito.verify(blackDuckApiClient, Mockito.never()).getAllResponses(Mockito.<BlackDuckMultipleRequest<BlackDuckVersionBomVulnerabilityView>>any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void retrieveVulnerabilitiesCallsCorrectEndpointTest() throws IntegrationException {
        Mockito.when(vulnerabilityDetailsCreator.hasSecurityRisk(Mockito.any())).thenReturn(true);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.<BlackDuckMultipleRequest<BlackDuckVersionBomVulnerabilityView>>any())).thenReturn(List.of());
        Mockito.when(vulnerabilityDetailsCreator.toComponentVulnerabilities(Mockito.any())).thenReturn(ComponentVulnerabilities.none());

        ProjectVersionComponentVersionView bomComponent = createBomComponent(BOM_COMPONENT_HREF, COMPONENT_VERSION_URL, false);
        bomComponentDetailsCreator.createBomComponentDetails(bomComponent, List.of(), ComponentUpgradeGuidance.none(), List.of());

        ArgumentCaptor<BlackDuckMultipleRequest<BlackDuckVersionBomVulnerabilityView>> captor = ArgumentCaptor.forClass(BlackDuckMultipleRequest.class);
        Mockito.verify(blackDuckApiClient, Mockito.times(1)).getAllResponses(captor.capture());

        BlackDuckMultipleRequest<BlackDuckVersionBomVulnerabilityView> spec = captor.getValue();
        String requestUrl = spec.getUrlResponse().getUrl().string();
        assertTrue(requestUrl.endsWith("/vulnerabilities"), "Expected endpoint URL to end with /vulnerabilities, but got: " + requestUrl);
        assertEquals(EXPECTED_VULN_ENDPOINT, requestUrl, "Unexpected vulnerabilities endpoint URL");
    }

    @Test
    void retrieveVulnerabilitiesPopulatesResultTest() throws IntegrationException {
        Mockito.when(vulnerabilityDetailsCreator.hasSecurityRisk(Mockito.any())).thenReturn(true);

        BlackDuckVersionBomVulnerabilityView highVuln = createVulnView("BDSA-HIGH", VulnerabilitySeverityType.HIGH, null);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.<BlackDuckMultipleRequest<BlackDuckVersionBomVulnerabilityView>>any())).thenReturn(List.of(highVuln));

        ComponentVulnerabilities expectedVulns = new ComponentVulnerabilities(
            List.of(),
            List.of(new LinkableItem("Vulnerability", "BDSA-HIGH", null)),
            List.of(),
            List.of()
        );
        Mockito.when(vulnerabilityDetailsCreator.toComponentVulnerabilities(List.of(highVuln))).thenReturn(expectedVulns);

        ProjectVersionComponentVersionView bomComponent = createBomComponent(BOM_COMPONENT_HREF, COMPONENT_VERSION_URL, false);
        BomComponentDetails bomComponentDetails = bomComponentDetailsCreator.createBomComponentDetails(bomComponent, List.of(), ComponentUpgradeGuidance.none(), List.of());

        assertTrue(bomComponentDetails.getComponentVulnerabilities().hasVulnerabilities(), "Expected vulnerabilities to be populated");
        assertEquals(1, bomComponentDetails.getComponentVulnerabilities().getHigh().size());
    }

    @Test
    void retrieveVulnerabilitiesEmptyResponseTest() throws IntegrationException {
        Mockito.when(vulnerabilityDetailsCreator.hasSecurityRisk(Mockito.any())).thenReturn(true);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.<BlackDuckMultipleRequest<BlackDuckVersionBomVulnerabilityView>>any())).thenReturn(List.of());
        Mockito.when(vulnerabilityDetailsCreator.toComponentVulnerabilities(List.of())).thenReturn(ComponentVulnerabilities.none());

        ProjectVersionComponentVersionView bomComponent = createBomComponent(BOM_COMPONENT_HREF, COMPONENT_VERSION_URL, false);
        BomComponentDetails bomComponentDetails = bomComponentDetailsCreator.createBomComponentDetails(bomComponent, List.of(), ComponentUpgradeGuidance.none(), List.of());

        assertFalse(bomComponentDetails.getComponentVulnerabilities().hasVulnerabilities(), "Expected no vulnerabilities for empty API response");
    }

    @Test
    void buildProjectVersionVulnerabilitiesUrlValidTest() throws IntegrationException {
        HttpUrl result = bomComponentDetailsCreator.buildProjectVersionVulnerabilitiesUrl(new HttpUrl(BOM_COMPONENT_HREF));
        assertEquals(EXPECTED_VULN_ENDPOINT, result.string(), "Expected project version vulnerabilities URL");
    }

    @Test
    void buildProjectVersionVulnerabilitiesUrlInvalidTest() throws IntegrationException {
        HttpUrl httpUrl = new HttpUrl("https://hub/api/no-components-segment/00000000-0000-0000-0000-000000000001");
        assertThrows(
            IntegrationException.class,
            () -> bomComponentDetailsCreator.buildProjectVersionVulnerabilitiesUrl(httpUrl),
            "Expected IntegrationException when href contains no /components/ segment"
        );
    }

    @Test
    void buildProjectVersionVulnerabilitiesUrlStripsQueryStringTest() throws IntegrationException {
        HttpUrl hrefWithQuery = new HttpUrl(BOM_COMPONENT_HREF + "?someParam=value");
        HttpUrl result = bomComponentDetailsCreator.buildProjectVersionVulnerabilitiesUrl(hrefWithQuery);
        assertEquals(EXPECTED_VULN_ENDPOINT, result.string(), "Expected query string to be stripped from derived vulnerabilities URL");
    }

    @Test
    void retrievePoliciesNotInViolationTest() throws IntegrationException {
        Mockito.when(vulnerabilityDetailsCreator.hasSecurityRisk(Mockito.any())).thenReturn(false);

        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, null);
        ProjectVersionComponentVersionView bomComponent = createBomComponent(BOM_COMPONENT_HREF, COMPONENT_VERSION_URL, false);
        BomComponentDetails bomComponentDetails = bomComponentDetailsCreator.createBomComponentDetails(
            bomComponent,
            List.of(policyConcern),
            ComponentUpgradeGuidance.none(),
            List.of()
        );

        assertTrue(bomComponentDetails.getRelevantPolicies().isEmpty(), "Expected no policies when component is not in violation");
        Mockito.verify(blackDuckApiClient, Mockito.never()).getAllResponses(Mockito.<UrlMultipleResponses<ComponentPolicyRulesView>>any());
    }

    @Test
    void retrievePoliciesInViolationNoPolicyConcernsTest() throws IntegrationException {
        Mockito.when(vulnerabilityDetailsCreator.hasSecurityRisk(Mockito.any())).thenReturn(false);

        ComponentConcern vulnerabilityConcern = ComponentConcern.vulnerability(ItemOperation.ADD, "BDSA-2025-0001", ComponentConcernSeverity.CRITICAL, null);
        ProjectVersionComponentVersionView bomComponent = createBomComponent(BOM_COMPONENT_HREF, COMPONENT_VERSION_URL, true);
        BomComponentDetails bomComponentDetails = bomComponentDetailsCreator.createBomComponentDetails(
            bomComponent,
            List.of(vulnerabilityConcern),
            ComponentUpgradeGuidance.none(),
            List.of()
        );

        assertTrue(bomComponentDetails.getRelevantPolicies().isEmpty(), "Expected no policies when no policy concerns are present");
        Mockito.verify(blackDuckApiClient, Mockito.never()).getAllResponses(Mockito.<UrlMultipleResponses<ComponentPolicyRulesView>>any());
    }

    @Test
    void retrievePoliciesInViolationWithMatchingConcernTest() throws IntegrationException {
        Mockito.when(vulnerabilityDetailsCreator.hasSecurityRisk(Mockito.any())).thenReturn(false);

        ComponentPolicyRulesView policyRulesView = new ComponentPolicyRulesView();
        policyRulesView.setName(POLICY_NAME);
        ResourceMetadata policyRulesMeta = new ResourceMetadata();
        policyRulesMeta.setHref(new HttpUrl("https://hub/api/policy-rules/test-policy"));
        policyRulesMeta.setLinks(List.of());
        policyRulesView.setMeta(policyRulesMeta);
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.<UrlMultipleResponses<ComponentPolicyRulesView>>any())).thenReturn(List.of(policyRulesView));

        ComponentPolicy expectedPolicy = new ComponentPolicy(POLICY_NAME, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, false, false, null, null);
        Mockito.when(policyDetailsCreator.toComponentPolicy(policyRulesView)).thenReturn(expectedPolicy);

        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, POLICY_NAME, null);
        ProjectVersionComponentVersionView bomComponent = createBomComponent(BOM_COMPONENT_HREF, COMPONENT_VERSION_URL, true);
        BomComponentDetails bomComponentDetails = bomComponentDetailsCreator.createBomComponentDetails(
            bomComponent,
            List.of(policyConcern),
            ComponentUpgradeGuidance.none(),
            List.of()
        );

        assertFalse(bomComponentDetails.getRelevantPolicies().isEmpty(), "Expected policies to be populated for matching concern");
        assertEquals(1, bomComponentDetails.getRelevantPolicies().size(), "Expected exactly one policy");
        assertEquals(POLICY_NAME, bomComponentDetails.getRelevantPolicies().get(0).getPolicyName(), "Expected matching policy name");
    }

    @Test
    void unknownVersionDetailsSkipsVulnerabilityApiCallTest() throws IntegrationException {
        ProjectVersionComponentVersionView bomComponent = createBomComponent(BOM_COMPONENT_HREF, COMPONENT_VERSION_URL, false);
        BomComponentDetails bomComponentDetails = bomComponentDetailsCreator.createBomComponentUnknownVersionDetails(
            bomComponent,
            List.of(),
            ComponentUpgradeGuidance.none(),
            List.of()
        );

        assertFalse(bomComponentDetails.getComponentVulnerabilities().hasVulnerabilities(), "Expected no vulnerabilities for unknown version");
        assertTrue(bomComponentDetails.getComponentVersion().isPresent(), "Expected component version to be present");
        assertEquals(
            BlackDuckMessageBomComponentDetailsCreator.COMPONENT_VERSION_UNKNOWN,
            bomComponentDetails.getComponentVersion().get().getValue(),
            "Expected unknown version label"
        );
        Mockito.verify(blackDuckApiClient, Mockito.never()).getAllResponses(Mockito.<BlackDuckMultipleRequest<BlackDuckVersionBomVulnerabilityView>>any());
        Mockito.verify(vulnerabilityDetailsCreator, Mockito.never()).hasSecurityRisk(Mockito.any());
    }

    private ProjectVersionComponentVersionView createBomComponent(String href, String componentVersionUrl, boolean policyInViolation) throws IntegrationException {
        ProjectVersionComponentVersionView view = new ProjectVersionComponentVersionView();
        view.setComponentName(COMPONENT_NAME);
        view.setComponentVersion(componentVersionUrl);
        view.setComponentVersionName(COMPONENT_VERSION_NAME);
        view.setPolicyStatus(policyInViolation
            ? ProjectVersionComponentPolicyStatusType.IN_VIOLATION
            : ProjectVersionComponentPolicyStatusType.NOT_IN_VIOLATION);

        ProjectVersionComponentVersionLicensesView licenseView = new ProjectVersionComponentVersionLicensesView();
        licenseView.setLicenseDisplay("Apache 2.0");
        view.setLicenses(List.of(licenseView));
        view.setUsages(List.of(UsageType.DYNAMICALLY_LINKED));

        RiskProfileCountsView riskCount = new RiskProfileCountsView();
        riskCount.setCountType(RiskPriorityType.HIGH);
        riskCount.setCount(BigDecimal.ONE);
        RiskProfileView riskProfile = new RiskProfileView();
        riskProfile.setCounts(List.of(riskCount));
        view.setSecurityRiskProfile(riskProfile);

        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl(href));
        if (policyInViolation) {
            ResourceLink policyRulesLink = new ResourceLink();
            policyRulesLink.setRel("policy-rules");
            policyRulesLink.setHref(new HttpUrl(href + "/policy-rules"));
            meta.setLinks(List.of(policyRulesLink));
        } else {
            meta.setLinks(List.of());
        }
        view.setMeta(meta);

        return view;
    }

    private BlackDuckVersionBomVulnerabilityView createVulnView(String id, VulnerabilitySeverityType severity, List<String> remediationStatuses) throws IntegrationException {
        ResourceLink link = new ResourceLink();
        link.setRel("vulnerability");
        link.setHref(new HttpUrl("https://hub/api/vulnerabilities/" + id));

        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("https://hub/api/projects/00000000/versions/00000001/vulnerabilities"));
        meta.setLinks(List.of(link));

        BlackDuckVersionBomVulnerabilityView view = new BlackDuckVersionBomVulnerabilityView();
        view.setId(id);
        view.setSeverity(severity);
        view.setRemediationStatus(remediationStatuses);
        view.setMeta(meta);
        return view;
    }

}
