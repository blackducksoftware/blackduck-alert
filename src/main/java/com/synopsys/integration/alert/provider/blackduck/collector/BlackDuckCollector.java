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
    // FIXME we should never store fields as optional
    private final Optional<BlackDuckBucketService> bucketService;
    private final Optional<BlackDuckService> blackDuckService;
    private final BlackDuckBucket blackDuckBucket;

    public BlackDuckCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList, final Collection<ProviderContentType> contentTypes, final BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, messageContentProcessorList, contentTypes);

        final Optional<BlackDuckServicesFactory> blackDuckServicesFactory = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger)
                                                                                .map(blackDuckHttpClient -> blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger)));
        blackDuckService = blackDuckServicesFactory.map(BlackDuckServicesFactory::createBlackDuckService);
        bucketService = blackDuckServicesFactory.map(BlackDuckServicesFactory::createBlackDuckBucketService);
        blackDuckBucket = new BlackDuckBucket();
    }

    public Optional<String> getProjectComponentQueryLink(final String projectVersionUrl, final String link, final String componentName) {
        final Optional<String> projectLink = getProjectLink(projectVersionUrl, link);
        return projectLink.flatMap(optionalProjectLink -> getProjectComponentQueryLink(optionalProjectLink, componentName));
    }

    public Optional<String> getProjectComponentQueryLink(final String projectLink, final String componentName) {
        return Optional.of(String.format("%s?q=componentName:%s", projectLink, componentName));
    }

    public Optional<String> getProjectLink(final String projectVersionUrl, final String link) {
        if (blackDuckService.isPresent() && bucketService.isPresent()) {
            try {
                final UriSingleResponse<ProjectVersionView> uriSingleResponse = new UriSingleResponse(projectVersionUrl, ProjectVersionView.class);
                final ProjectVersionView projectVersionView = (blackDuckBucket.contains(uriSingleResponse.getUri())) ? blackDuckBucket.get(uriSingleResponse) : blackDuckService.get().getResponse(projectVersionUrl, ProjectVersionView.class);
                bucketService.get().addToTheBucket(blackDuckBucket, List.of(uriSingleResponse));
                return projectVersionView.getFirstLink(link);
            } catch (final IntegrationException e) {
                logger.error("There was a problem retrieving the Project Version link.", e);
            }
        }

        return Optional.empty();
    }

    public Optional<VersionBomComponentView> getBomComponentView(final String bomComponentUrl) {
        if (getBucketService().isPresent()) {
            try {
                getBucketService().get().addToTheBucket(getBlackDuckBucket(), bomComponentUrl, VersionBomComponentView.class);
                return Optional.ofNullable(getBlackDuckBucket().get(bomComponentUrl, VersionBomComponentView.class));
            } catch (final IntegrationException ie) {
                logger.error("Error retrieving bom component/", ie);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public List<LinkableItem> getLicenseLinkableItems(final VersionBomComponentView bomComponentView) {
        return bomComponentView.getLicenses().stream().map(licenseView -> {
            LinkableItem item = new LinkableItem(BlackDuckContent.LABEL_COMPONENT_LICENSE, licenseView.getLicenseDisplay(), licenseView.getLicense());
            item.setSummarizable(true);
            item.setCollapsible(true);
            return item;
        }).collect(Collectors.toList());
    }

    public Optional<BlackDuckBucketService> getBucketService() {
        return bucketService;
    }

    public Optional<BlackDuckService> getBlackDuckService() {
        return blackDuckService;
    }

    public BlackDuckBucket getBlackDuckBucket() {
        return blackDuckBucket;
    }
}
