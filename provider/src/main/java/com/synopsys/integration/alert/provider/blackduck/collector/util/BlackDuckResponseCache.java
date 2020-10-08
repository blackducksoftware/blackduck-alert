/**
 * provider
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.collector.util;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckResponseCache {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckResponseCache.class);
    private final BlackDuckBucketService blackDuckBucketService;
    private final BlackDuckBucket bucket;
    private final long timeout;

    public BlackDuckResponseCache(BlackDuckBucketService blackDuckBucketService, BlackDuckBucket bucket, long timeout) {
        this.blackDuckBucketService = blackDuckBucketService;
        this.bucket = bucket;
        this.timeout = timeout;
    }

    public <T extends BlackDuckResponse> Optional<T> getItem(Class<T> responseClass, String url) {
        if (null == responseClass || StringUtils.isBlank(url)) {
            return Optional.empty();
        }
        try {
            Future<Optional<T>> optionalProjectVersionFuture = blackDuckBucketService.addToTheBucket(bucket, url, responseClass);
            if (BooleanUtils.isTrue(bucket.hasAnyErrors())) {
                Optional<Exception> error = bucket.getError(url);
                error.ifPresent(exception -> {
                    logger.debug(String.format("There was a problem retrieving the link '%s'. Error: %s", url, exception.getMessage()));
                    logger.trace(exception.getMessage(), exception);
                });
            }
            if (null != optionalProjectVersionFuture) {
                return optionalProjectVersionFuture
                           .get(timeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException interruptedException) {
            logger.debug("The thread was interrupted, failing safely...");
            Thread.currentThread().interrupt();
        } catch (Exception genericException) {
            logger.error(String.format("There was a problem retrieving the link '%s'.", url), genericException);
        }

        return Optional.empty();
    }

    public Optional<String> getProjectComponentQueryLink(String projectVersionUrl, String link, String componentName) {
        Optional<String> projectLink = getProjectLink(projectVersionUrl, link);
        return projectLink.map(optionalProjectLink -> getProjectComponentQueryLink(optionalProjectLink, componentName));
    }

    public String getProjectComponentQueryLink(String projectLink, String componentName) {
        return String.format("%s?q=componentName:%s", projectLink, componentName);
    }

    public Optional<String> getProjectLink(String projectVersionUrl, String link) {
        Optional<ProjectVersionView> optionalProjectVersionFuture = getItem(ProjectVersionView.class, projectVersionUrl);
        return optionalProjectVersionFuture
                   .map(view -> view.getFirstLink(link))
                   .map(HttpUrl::toString);
    }

    public Optional<ProjectVersionComponentView> getBomComponentView(String bomComponentUrl) {
        if (StringUtils.isNotBlank(bomComponentUrl)) {
            return getItem(ProjectVersionComponentView.class, bomComponentUrl);
        }
        return Optional.empty();
    }

    public Optional<PolicyRuleView> getPolicyRule(BlackDuckResponseCache blackDuckResponseCache, PolicyInfo policyInfo) {
        try {
            String policyUrl = policyInfo.getPolicy();
            if (StringUtils.isNotBlank(policyUrl)) {
                return blackDuckResponseCache.getItem(PolicyRuleView.class, policyUrl);
            }
        } catch (Exception e) {
            logger.debug("Unable to get policy rule: {}", policyInfo.getPolicyName());
            logger.debug("Cause:", e);
        }
        return Optional.empty();
    }

    public Optional<ProjectVersionWrapper> getProjectVersionWrapper(String projectVersionUrl) {
        Optional<ProjectVersionView> projectVersion = getItem(ProjectVersionView.class, projectVersionUrl);
        ProjectVersionWrapper wrapper = new ProjectVersionWrapper();
        projectVersion.ifPresent(wrapper::setProjectVersionView);
        projectVersion.flatMap(version -> getItem(ProjectView.class, version.getFirstLink(ProjectVersionView.PROJECT_LINK).toString()))
            .ifPresent(wrapper::setProjectView);
        return Optional.of(wrapper);
    }

}
