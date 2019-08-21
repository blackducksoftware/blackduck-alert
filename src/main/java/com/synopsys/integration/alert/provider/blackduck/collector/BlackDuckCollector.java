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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckDataHelper;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.log.Slf4jIntLogger;

// Created this class as a parent because of the ObjectFactory bean that is used with Collectors which destroys the bean after use. These services need to be destroyed after usage.
public abstract class BlackDuckCollector extends MessageContentCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckBucketService bucketService;
    private final BlackDuckService blackDuckService;
    private final BlackDuckBucket blackDuckBucket;
    private final BlackDuckDataHelper blackDuckDataHelper;

    public BlackDuckCollector(JsonExtractor jsonExtractor, Collection<ProviderContentType> contentTypes, BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, contentTypes);
        this.blackDuckProperties = blackDuckProperties;

        Optional<BlackDuckServicesFactory> blackDuckServicesFactory = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger)
                                                                          .map(blackDuckHttpClient -> blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger)));
        blackDuckService = blackDuckServicesFactory
                               .map(BlackDuckServicesFactory::createBlackDuckService)
                               .orElseThrow(() -> new AlertRuntimeException("The BlackDuckCollector cannot be used without a valid Black Duck connection"));
        bucketService = blackDuckServicesFactory
                            .map(BlackDuckServicesFactory::createBlackDuckBucketService)
                            .orElseThrow(() -> new AlertRuntimeException("The BlackDuckCollector cannot be used without a valid Black Duck connection"));
        blackDuckBucket = new BlackDuckBucket();
        blackDuckDataHelper = new BlackDuckDataHelper(blackDuckProperties, blackDuckService, blackDuckBucket, bucketService);
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

    public BlackDuckDataHelper getBlackDuckDataHelper() {
        return blackDuckDataHelper;
    }

    @Override
    protected LinkableItem getProviderItem() {
        final String blackDuckUrl = blackDuckProperties.getBlackDuckUrl().orElse(null);
        return new LinkableItem(ProviderMessageContent.LABEL_PROVIDER, "Black Duck", blackDuckUrl);
    }

}
