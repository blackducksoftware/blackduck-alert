/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.generated.component.RemediatingVersionView;
import com.synopsys.integration.blackduck.api.generated.response.RemediationOptionsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

// Created this class as a parent because of the ObjectFactory bean that is used with Collectors which destroys the bean after use. These services need to be destroyed after usage.
public abstract class BlackDuckCollector extends MessageContentCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckBucketService bucketService;
    private final BlackDuckService blackDuckService;
    private final BlackDuckBucket blackDuckBucket;

    public BlackDuckCollector(final JsonExtractor jsonExtractor, final Collection<ProviderContentType> contentTypes, final BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, contentTypes);
        this.blackDuckProperties = blackDuckProperties;

        final Optional<BlackDuckServicesFactory> blackDuckServicesFactory = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger)
                                                                                .map(blackDuckHttpClient -> blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger)));
        blackDuckService = blackDuckServicesFactory
                               .map(BlackDuckServicesFactory::createBlackDuckService)
                               .orElseThrow(() -> new AlertRuntimeException("The BlackDuckCollector cannot be used without a valid Black Duck connection"));
        bucketService = blackDuckServicesFactory
                            .map(BlackDuckServicesFactory::createBlackDuckBucketService)
                            .orElseThrow(() -> new AlertRuntimeException("The BlackDuckCollector cannot be used without a valid Black Duck connection"));
        blackDuckBucket = new BlackDuckBucket();
    }

    @Override
    protected LinkableItem getProviderItem() {
        final String blackDuckUrl = blackDuckProperties.getBlackDuckUrl().orElse(null);
        return new LinkableItem(ProviderMessageContent.LABEL_PROVIDER, "Black Duck", blackDuckUrl);
    }

    public Optional<String> getProjectComponentQueryLink(final String projectVersionUrl, final String link, final String componentName) {
        final Optional<String> projectLink = getProjectLink(projectVersionUrl, link);
        return projectLink.flatMap(optionalProjectLink -> getProjectComponentQueryLink(optionalProjectLink, componentName));
    }

    public Optional<String> getProjectComponentQueryLink(final String projectLink, final String componentName) {
        return Optional.of(String.format("%s?q=componentName:%s", projectLink, componentName));
    }

    public Optional<String> getProjectLink(final String projectVersionUrl, final String link) {
        try {
            final Future<Optional<ProjectVersionView>> optionalProjectVersionFuture = bucketService.addToTheBucket(blackDuckBucket, projectVersionUrl, ProjectVersionView.class);
            return optionalProjectVersionFuture
                       .get(blackDuckProperties.getBlackDuckTimeout(), TimeUnit.SECONDS)
                       .flatMap(view -> view.getFirstLink(link));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("There was a problem retrieving the Project Version link.", e);
        }

        return Optional.empty();
    }

    public Optional<VersionBomComponentView> getBomComponentView(final String bomComponentUrl) {
        try {
            final Future<Optional<VersionBomComponentView>> optionalVersionBomComponentFuture = bucketService.addToTheBucket(getBlackDuckBucket(), bomComponentUrl, VersionBomComponentView.class);
            return optionalVersionBomComponentFuture.get(blackDuckProperties.getBlackDuckTimeout(), TimeUnit.SECONDS);
        } catch (final Exception e) {
            logger.error("Error retrieving bom component", e);
        }
        return Optional.empty();
    }

    public List<LinkableItem> getLicenseLinkableItems(final VersionBomComponentView bomComponentView) {
        return bomComponentView.getLicenses()
                   .stream()
                   .map(licenseView -> {
                       // blackduck displays the license data in a modal dialog.  Therefore a link to the license doesn't make sense.
                       // Also the VersionBomLicenseView doesn't have any link mappings to the text link.
                       LinkableItem item = new LinkableItem(BlackDuckContent.LABEL_COMPONENT_LICENSE, licenseView.getLicenseDisplay());
                       item.setCollapsible(true);
                       return item;
                   })
                   .collect(Collectors.toList());
    }

    public List<LinkableItem> getRemediationItems(ComponentVersionView componentVersionView) throws IntegrationException {
        List<LinkableItem> remediationItems = new LinkedList<>();
        ComponentService componentService = new ComponentService(getBlackDuckService(), new Slf4jIntLogger(logger));
        Optional<RemediationOptionsView> optionalRemediation = componentService.getRemediationInformation(componentVersionView);
        if (optionalRemediation.isPresent()) {
            RemediationOptionsView remediationOptions = optionalRemediation.get();
            if (null != remediationOptions.getFixesPreviousVulnerabilities()) {
                RemediatingVersionView remediatingVersionView = remediationOptions.getFixesPreviousVulnerabilities();
                String versionText = createRemediationVersionText(remediatingVersionView);
                remediationItems.add(new LinkableItem(BlackDuckContent.LABEL_REMEDIATION_FIX_PREVIOUS, versionText, remediatingVersionView.getComponentVersion()));
            }
            if (null != remediationOptions.getLatestAfterCurrent()) {
                RemediatingVersionView remediatingVersionView = remediationOptions.getLatestAfterCurrent();
                String versionText = createRemediationVersionText(remediatingVersionView);
                remediationItems.add(new LinkableItem(BlackDuckContent.LABEL_REMEDIATION_LATEST, versionText, remediatingVersionView.getComponentVersion()));
            }
            if (null != remediationOptions.getNoVulnerabilities()) {
                RemediatingVersionView remediatingVersionView = remediationOptions.getNoVulnerabilities();
                String versionText = createRemediationVersionText(remediatingVersionView);
                remediationItems.add(new LinkableItem(BlackDuckContent.LABEL_REMEDIATION_CLEAN, versionText, remediatingVersionView.getComponentVersion()));
            }
        }
        return remediationItems;
    }

    public BlackDuckService getBlackDuckService() {
        return blackDuckService;
    }

    protected BlackDuckBucketService getBucketService() {
        return bucketService;
    }

    protected BlackDuckBucket getBlackDuckBucket() {
        return blackDuckBucket;
    }

    private String createRemediationVersionText(final RemediatingVersionView remediatingVersionView) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(remediatingVersionView.getName());
        if (remediatingVersionView.getVulnerabilityCount() != null && remediatingVersionView.getVulnerabilityCount() > 0) {
            stringBuilder.append(" (Vulnerability Count: ");
            stringBuilder.append(remediatingVersionView.getVulnerabilityCount());
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }

}
