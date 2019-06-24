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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
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
    private final boolean hasValidConnection;

    public BlackDuckCollector(JsonExtractor jsonExtractor, List<MessageContentProcessor> messageContentProcessorList, Collection<ProviderContentType> contentTypes, BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, messageContentProcessorList, contentTypes);
        this.blackDuckProperties = blackDuckProperties;

        final Optional<BlackDuckServicesFactory> blackDuckServicesFactory = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger)
                                                                                .map(blackDuckHttpClient -> blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger)));
        blackDuckService = blackDuckServicesFactory
                               .map(BlackDuckServicesFactory::createBlackDuckService)
                               .orElse(null);
        bucketService = blackDuckServicesFactory
                            .map(BlackDuckServicesFactory::createBlackDuckBucketService)
                            .orElse(null);
        hasValidConnection = null != blackDuckService && null != bucketService;
        blackDuckBucket = new BlackDuckBucket();
    }

    @Override
    protected LinkableItem getProviderItem() {
        String blackDuckUrl = blackDuckProperties.getBlackDuckUrl().orElse(null);
        return new LinkableItem(ProviderMessageContent.LABEL_PROVIDER, "Black Duck", blackDuckUrl);
    }

    public Optional<String> getProjectComponentQueryLink(String projectVersionUrl, String link, String componentName) {
        final Optional<String> projectLink = getProjectLink(projectVersionUrl, link);
        return projectLink.flatMap(optionalProjectLink -> getProjectComponentQueryLink(optionalProjectLink, componentName));
    }

    public Optional<String> getProjectComponentQueryLink(String projectLink, String componentName) {
        return Optional.of(String.format("%s?q=componentName:%s", projectLink, componentName));
    }

    public Optional<String> getProjectLink(String projectVersionUrl, String link) {
        if (hasValidConnection) {
            try {
                final UriSingleResponse<ProjectVersionView> uriSingleResponse = new UriSingleResponse(projectVersionUrl, ProjectVersionView.class);
                final ProjectVersionView projectVersionView = (blackDuckBucket.contains(uriSingleResponse.getUri())) ? blackDuckBucket.get(uriSingleResponse) : blackDuckService.getResponse(projectVersionUrl, ProjectVersionView.class);
                bucketService.addToTheBucket(blackDuckBucket, List.of(uriSingleResponse));
                return projectVersionView.getFirstLink(link);
            } catch (final IntegrationException e) {
                logger.error("There was a problem retrieving the Project Version link.", e);
            }
        }

        return Optional.empty();
    }

    public Optional<VersionBomComponentView> getBomComponentView(String bomComponentUrl) {
        if (hasValidConnection) {
            try {
                bucketService.addToTheBucket(getBlackDuckBucket(), bomComponentUrl, VersionBomComponentView.class);
                return Optional.ofNullable(getBlackDuckBucket().get(bomComponentUrl, VersionBomComponentView.class));
            } catch (final IntegrationException ie) {
                logger.error("Error retrieving bom component/", ie);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public List<LinkableItem> getLicenseLinkableItems(VersionBomComponentView bomComponentView) {
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

    protected boolean hasValidConnection() {
        return hasValidConnection;
    }

    protected Optional<BlackDuckService> getBlackDuckService() {
        return Optional.ofNullable(blackDuckService);
    }

    protected Optional<BlackDuckBucketService> getBucketService() {
        return Optional.ofNullable(bucketService);
    }

    protected BlackDuckBucket getBlackDuckBucket() {
        return blackDuckBucket;
    }
}
