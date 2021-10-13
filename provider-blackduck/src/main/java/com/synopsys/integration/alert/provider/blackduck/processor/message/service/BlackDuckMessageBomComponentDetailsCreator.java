/*
 * provider-blackduck
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.service;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageAttributesUtils;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageLinkUtils;
import com.synopsys.integration.blackduck.api.core.response.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.VersionBomOriginView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckMessageBomComponentDetailsCreator {
    private static final String ORIGIN_SPEC = "/origins";
    private static final LinkMultipleResponses<BlackDuckProjectVersionComponentVulnerabilitiesView> VULNERABILITIES_LINK =
        new LinkMultipleResponses<>("vulnerabilities", BlackDuckProjectVersionComponentVulnerabilitiesView.class);
    private static final String VULNERABILITIES_MEDIA_TYPE = "application/vnd.blackducksoftware.internal-1+json";
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

    public BomComponentDetails createBomComponentDetails(ProjectVersionComponentVersionView bomComponent, ComponentConcern componentConcern, List<LinkableItem> additionalAttributes) throws IntegrationException {
        return createBomComponentDetails(bomComponent, List.of(componentConcern), additionalAttributes);
    }

    public BomComponentDetails createBomComponentDetails(ProjectVersionComponentVersionView bomComponent, List<ComponentConcern> componentConcerns, List<LinkableItem> additionalAttributes) throws IntegrationException {
        LinkableItem component;
        LinkableItem componentVersion = null;

        // FIXME using this query link only in a successful result and not in an unsuccessful result leads to inconsistent values in our custom fields which leads to inconsistent search results (bug).
        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(bomComponent);

        String componentVersionUrl = bomComponent.getComponent();
        if (StringUtils.isNotBlank(componentVersionUrl)) {
            component = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT, bomComponent.getComponentName());
            componentVersion = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT_VERSION, bomComponent.getComponentName(), componentQueryLink);
        } else {
            component = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT, bomComponent.getComponentName(), componentQueryLink);
        }

        ComponentVulnerabilities componentVulnerabilities = retrieveComponentVulnerabilities(bomComponent);
        List<ComponentPolicy> componentPolicies = retrieveComponentPolicies(bomComponent);

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
            additionalAttributes,
            issuesUrl
        );
    }

    public BomComponentDetails createBomComponentUnknownVersionDetails(ProjectVersionComponentVersionView bomComponent, List<ComponentConcern> componentConcerns, List<LinkableItem> additionalAttributes) throws IntegrationException {
        // FIXME using this query link only in a successful result and not in an unsuccessful result leads to inconsistent values in our custom fields which leads to inconsistent search results (bug).
        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(bomComponent);

        LinkableItem component = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT, bomComponent.getComponentName(), componentQueryLink);
        LinkableItem componentVersion = new LinkableItem(BlackDuckMessageLabels.LABEL_COMPONENT_VERSION, COMPONENT_VERSION_UNKNOWN);

        ComponentVulnerabilities componentVulnerabilities = ComponentVulnerabilities.none();
        List<ComponentPolicy> componentPolicies = retrieveComponentPolicies(bomComponent);

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
        List<LinkableItem> additionalAttributes
    ) {
        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(componentUrl, componentName);

        return createMissingDetails(
            componentName,
            () -> componentQueryLink,
            componentVersionName,
            () -> componentQueryLink,
            componentConcerns,
            additionalAttributes
        );
    }

    // This exists due to an issue with searching for the wrong URL in an Azure search property. More info here IALERT-2654
    public BomComponentDetails createMissingBomComponentDetailsForUnknownVersion(
        String componentName,
        @Nullable String componentUrl,
        @Nullable String componentVersionName,
        List<ComponentConcern> componentConcerns,
        List<LinkableItem> additionalAttributes
    ) {
        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(componentUrl, componentName);

        return createMissingDetails(
            componentName,
            () -> componentQueryLink,
            componentVersionName,
            () -> null,
            componentConcerns,
            additionalAttributes
        );
    }

    public BomComponentDetails createMissingBomComponentDetails(
        String componentName,
        @Nullable String componentUrl,
        @Nullable String componentVersionName,
        @Nullable String componentVersionUrl,
        List<ComponentConcern> componentConcerns,
        List<LinkableItem> additionalAttributes
    ) {

        return createMissingDetails(
            componentName,
            () -> componentUrl,
            componentVersionName,
            () -> componentVersionUrl,
            componentConcerns,
            additionalAttributes
        );
    }

    private BomComponentDetails createMissingDetails(
        String componentName,
        Supplier<String> componentUrlRetriever,
        @Nullable String componentVersionName,
        Supplier<String> componentVersionUrlRetriever,
        List<ComponentConcern> componentConcerns,
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
            additionalAttributes,
            null
        );
    }

    private ComponentVulnerabilities retrieveComponentVulnerabilities(ProjectVersionComponentVersionView bomComponent) throws IntegrationException {
        if (!vulnerabilityDetailsCreator.hasSecurityRisk(bomComponent)) {
            return ComponentVulnerabilities.none();
        }

        HttpUrl vulnerabilitiesUrl = createVulnerabilitiesLink(bomComponent);
        UrlMultipleResponses<BlackDuckProjectVersionComponentVulnerabilitiesView> urlMultipleResponses = new UrlMultipleResponses<>(vulnerabilitiesUrl, VULNERABILITIES_LINK.getResponseClass());
        BlackDuckMultipleRequest<BlackDuckProjectVersionComponentVulnerabilitiesView> spec = new BlackDuckRequestBuilder()
            .commonGet()
            .addHeader(HttpHeaders.ACCEPT, VULNERABILITIES_MEDIA_TYPE)
            .buildBlackDuckRequest(urlMultipleResponses);

        List<BlackDuckProjectVersionComponentVulnerabilitiesView> vulnerabilities = blackDuckApiClient.getAllResponses(spec);
        return vulnerabilityDetailsCreator.toComponentVulnerabilities(vulnerabilities);
    }

    private List<ComponentPolicy> retrieveComponentPolicies(ProjectVersionComponentVersionView bomComponent) throws IntegrationException {
        if (ProjectVersionComponentPolicyStatusType.NOT_IN_VIOLATION.equals(bomComponent.getPolicyStatus())) {
            return List.of();
        }
        return blackDuckApiClient.getAllResponses(bomComponent.metaPolicyRulesLink())
            .stream()
            .map(policyDetailsCreator::toComponentPolicy)
            .collect(Collectors.toList());
    }

    private HttpUrl createVulnerabilitiesLink(ProjectVersionComponentVersionView bomComponent) throws IntegrationException {
        HttpUrl vulnerabilitiesUrl = bomComponent.getHref();

        List<VersionBomOriginView> origins = bomComponent.getOrigins();
        // TODO determine what to do when there are multiple origins
        if (null != origins && origins.size() == 1) {
            VersionBomOriginView singleOrigin = origins.get(0);
            String originUrl = singleOrigin.getOrigin();
            if (StringUtils.isNotBlank(originUrl)) {
                String originId = StringUtils.substringAfterLast(originUrl, ORIGIN_SPEC);
                vulnerabilitiesUrl = vulnerabilitiesUrl.appendRelativeUrl(ORIGIN_SPEC).appendRelativeUrl(originId);
            }
        }
        return vulnerabilitiesUrl.appendRelativeUrl(VULNERABILITIES_LINK.getLink());
    }

}
