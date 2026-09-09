/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentPolicy;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageAttributesUtils;
import com.blackduck.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageLinkUtils;
import com.blackduck.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.blackduck.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.blackduck.integration.blackduck.api.generated.view.ComponentPolicyRulesView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;

public class BlackDuckMessageBomComponentDetailsCreator {
    private static final String VULNERABILITIES_MEDIA_TYPE = "application/vnd.blackducksoftware.bill-of-materials-8+json";
    private static final String BOM_COMPONENT_FILTER_KEY = "bomComponents";
    private static final String VULNERABILITIES_PATH = "/vulnerabilities";
    public static final String COMPONENT_VERSION_UNKNOWN = "Unknown Version";

    private final BlackDuckApiClient blackDuckApiClient;
    private final BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator;
    private final BlackDuckComponentPolicyDetailsCreator policyDetailsCreator;

    public BlackDuckMessageBomComponentDetailsCreator(
        BlackDuckApiClient blackDuckApiClient,
        BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator,
        BlackDuckComponentPolicyDetailsCreator policyDetailsCreator
    ) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.vulnerabilityDetailsCreator = vulnerabilityDetailsCreator;
        this.policyDetailsCreator = policyDetailsCreator;
    }

    public BomComponentDetails createBomComponentDetails(
        ProjectVersionComponentVersionView bomComponent,
        ComponentConcern componentConcern,
        ComponentUpgradeGuidance componentUpgradeGuidance,
        List<LinkableItem> additionalAttributes
    ) throws IntegrationException {
        return createBomComponentDetails(bomComponent, List.of(componentConcern), componentUpgradeGuidance, additionalAttributes);
    }

    public BomComponentDetails createBomComponentDetails(
        ProjectVersionComponentVersionView bomComponent,
        List<ComponentConcern> componentConcerns,
        ComponentUpgradeGuidance componentUpgradeGuidance,
        List<LinkableItem> additionalAttributes
    ) throws IntegrationException {
        LinkableItem component;
        LinkableItem componentVersion = null;

        // FIXME using this query link only in a successful result and not in an unsuccessful result leads to inconsistent values in our custom fields which leads to inconsistent search results (bug).
        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(bomComponent);

        String componentVersionUrl = bomComponent.getComponentVersion();
        if (StringUtils.isNotBlank(componentVersionUrl)) {
            component = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT, bomComponent.getComponentName());
            componentVersion = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT_VERSION, bomComponent.getComponentVersionName(), componentQueryLink);
        } else {
            component = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT, bomComponent.getComponentName(), componentQueryLink);
        }

        ComponentVulnerabilities componentVulnerabilities = retrieveComponentVulnerabilities(bomComponent);
        List<ComponentPolicy> componentPolicies = retrieveComponentPolicies(bomComponent, componentConcerns);

        LinkableItem licenseInfo = BlackDuckMessageAttributesUtils.extractLicense(bomComponent);
        String usageInfo = BlackDuckMessageAttributesUtils.extractUsage(bomComponent);
        String issuesUrl = BlackDuckMessageAttributesUtils.extractIssuesUrl(bomComponent).orElse(null);

        return new BomComponentDetails(
            component,
            componentVersion,
            componentVulnerabilities,
            componentPolicies,
            componentConcerns,
            licenseInfo,
            usageInfo,
            componentUpgradeGuidance,
            additionalAttributes,
            issuesUrl
        );
    }

    public BomComponentDetails createBomComponentUnknownVersionDetails(
        ProjectVersionComponentVersionView bomComponent, List<ComponentConcern> componentConcerns, ComponentUpgradeGuidance componentUpgradeGuidance,
        List<LinkableItem> additionalAttributes
    ) throws IntegrationException {
        // FIXME using this query link only in a successful result and not in an unsuccessful result leads to inconsistent values in our custom fields which leads to inconsistent search results (bug).
        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(bomComponent);

        LinkableItem component = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT, bomComponent.getComponentName(), componentQueryLink);
        LinkableItem componentVersion = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT_VERSION, COMPONENT_VERSION_UNKNOWN);

        ComponentVulnerabilities componentVulnerabilities = ComponentVulnerabilities.none();
        List<ComponentPolicy> componentPolicies = retrieveComponentPolicies(bomComponent, componentConcerns);

        LinkableItem licenseInfo = BlackDuckMessageAttributesUtils.extractLicense(bomComponent);
        String usageInfo = BlackDuckMessageAttributesUtils.extractUsage(bomComponent);
        String issuesUrl = BlackDuckMessageAttributesUtils.extractIssuesUrl(bomComponent).orElse(null);

        return new BomComponentDetails(
            component,
            componentVersion,
            componentVulnerabilities,
            componentPolicies,
            componentConcerns,
            licenseInfo,
            usageInfo,
            componentUpgradeGuidance,
            additionalAttributes,
            issuesUrl
        );
    }

    // This exists due to an issue with searching for the wrong URL in an Azure search property. More info here IALERT-2654
    public BomComponentDetails createMissingBomComponentDetailsForVulnerability(
        String componentName,
        @Nullable String componentUrl,
        @Nullable String componentVersionName,
        List<ComponentConcern> componentConcerns,
        ComponentUpgradeGuidance componentUpgradeGuidance,
        List<LinkableItem> additionalAttributes
    ) {
        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(componentUrl, componentName);

        return createMissingDetails(
            componentName,
            () -> componentQueryLink,
            componentVersionName,
            () -> componentQueryLink,
            componentConcerns,
            componentUpgradeGuidance,
            additionalAttributes
        );
    }

    // This exists due to an issue with searching for the wrong URL in an Azure search property. More info here IALERT-2654
    public BomComponentDetails createMissingBomComponentDetailsForUnknownVersion(
        String componentName,
        @Nullable String componentUrl,
        @Nullable String componentVersionName,
        List<ComponentConcern> componentConcerns,
        ComponentUpgradeGuidance componentUpgradeGuidance,
        List<LinkableItem> additionalAttributes
    ) {
        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(componentUrl, componentName);

        return createMissingDetails(
            componentName,
            () -> componentQueryLink,
            componentVersionName,
            () -> null,
            componentConcerns,
            componentUpgradeGuidance,
            additionalAttributes
        );
    }

    public BomComponentDetails createMissingBomComponentDetails(
        String componentName,
        @Nullable String componentUrl,
        @Nullable String componentVersionName,
        @Nullable String componentVersionUrl,
        List<ComponentConcern> componentConcerns,
        ComponentUpgradeGuidance componentUpgradeGuidance,
        List<LinkableItem> additionalAttributes
    ) {

        return createMissingDetails(
            componentName,
            () -> componentUrl,
            componentVersionName,
            () -> componentVersionUrl,
            componentConcerns,
            componentUpgradeGuidance,
            additionalAttributes
        );
    }

    private BomComponentDetails createMissingDetails(
        String componentName,
        Supplier<String> componentUrlRetriever,
        @Nullable String componentVersionName,
        Supplier<String> componentVersionUrlRetriever,
        List<ComponentConcern> componentConcerns,
        ComponentUpgradeGuidance componentUpgradeGuidance,
        List<LinkableItem> additionalAttributes
    ) {
        LinkableItem component;
        LinkableItem componentVersion = null;

        String componentVersionUrl = componentVersionUrlRetriever.get();

        if (StringUtils.isNotBlank(componentVersionUrl)) {
            component = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT, componentName);
            componentVersion = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT_VERSION, componentVersionName, componentVersionUrl);
        } else {
            component = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT, componentName, componentUrlRetriever.get());
        }

        LinkableItem licenseInfo = new LinkableItem(BlackDuckMessageLabels.LABEL_LICENSE, BlackDuckMessageLabels.VALUE_UNKNOWN_LICENSE);
        String usageInfo = BlackDuckMessageLabels.VALUE_UNKNOWN_USAGE;

        return new BomComponentDetails(
            component,
            componentVersion,
            ComponentVulnerabilities.none(),
            List.of(),
            componentConcerns,
            licenseInfo,
            usageInfo,
            componentUpgradeGuidance,
            additionalAttributes,
            null
        );
    }

    private ComponentVulnerabilities retrieveComponentVulnerabilities(ProjectVersionComponentVersionView bomComponent) throws IntegrationException {
        if (!vulnerabilityDetailsCreator.hasSecurityRisk(bomComponent)) {
            return ComponentVulnerabilities.none();
        }

        String componentVersionUrl = bomComponent.getComponentVersion();
        if (StringUtils.isBlank(componentVersionUrl)) {
            return ComponentVulnerabilities.none();
        }

        String projectVersionUrl = BlackDuckMessageLinkUtils.createProjectVersionLink(bomComponent);
        HttpUrl vulnerabilitiesEndpointUrl = new HttpUrl(projectVersionUrl + VULNERABILITIES_PATH);
        // TODO: The bom component response does not yet include a pre-encoded component version URL suitable for the
        // bomComponents filter. Until it does, the URL is encoded manually here. When Hub provides the encoded value
        // directly this manual encoding should become a fallback for backwards compatibility.
        String encodedComponentVersionUrl = Base64.getUrlEncoder().encodeToString(componentVersionUrl.getBytes(StandardCharsets.UTF_8));
        String filterValue = BOM_COMPONENT_FILTER_KEY + ":" + encodedComponentVersionUrl;

        UrlMultipleResponses<BlackDuckVersionBomVulnerabilityView> urlMultipleResponses =
            new UrlMultipleResponses<>(vulnerabilitiesEndpointUrl, BlackDuckVersionBomVulnerabilityView.class);
        BlackDuckMultipleRequest<BlackDuckVersionBomVulnerabilityView> spec = new BlackDuckRequestBuilder()
            .commonGet()
            .addHeader(HttpHeaders.ACCEPT, VULNERABILITIES_MEDIA_TYPE)
            .addQueryParameter("filter", filterValue)
            .buildBlackDuckRequest(urlMultipleResponses);

        List<BlackDuckVersionBomVulnerabilityView> vulnerabilityViews = blackDuckApiClient.getAllResponses(spec);
        return vulnerabilityDetailsCreator.toComponentVulnerabilities(vulnerabilityViews);
    }

    private List<ComponentPolicy> retrieveComponentPolicies(ProjectVersionComponentVersionView bomComponent, List<ComponentConcern> componentConcerns) throws IntegrationException {
        if (ProjectVersionComponentPolicyStatusType.NOT_IN_VIOLATION.equals(bomComponent.getPolicyStatus())) {
            return List.of();
        }

        List<ComponentConcern> policyConcerns = componentConcerns
            .stream()
            .filter(compConcern -> ComponentConcernType.POLICY.equals(compConcern.getType()))
            .toList();
        if (policyConcerns.isEmpty()) {
            return List.of();
        }

        return blackDuckApiClient.getAllResponses(bomComponent.metaPolicyRulesLink())
            .stream()
            .filter(policyRulesView -> hasConcernForPolicy(policyRulesView, policyConcerns))
            .map(policyDetailsCreator::toComponentPolicy)
            .toList();
    }

    private boolean hasConcernForPolicy(ComponentPolicyRulesView policyRulesView, List<ComponentConcern> policyConcerns) {
        for (ComponentConcern policyConcern : policyConcerns) {
            Optional<String> optionalUrl = policyConcern.getUrl();
            HttpUrl policyRulesViewHref = policyRulesView.getHref();
            if (null != policyRulesViewHref && optionalUrl.isPresent()) {
                String componentConcernPolicyUrl = optionalUrl.get();
                if (componentConcernPolicyUrl.equals(policyRulesViewHref.string())) {
                    return true;
                }
            } else if (policyConcern.getName().equals(policyRulesView.getName())) {
                return true;
            }
        }
        return false;
    }

}
