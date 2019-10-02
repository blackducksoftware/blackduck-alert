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
package com.synopsys.integration.alert.provider.blackduck.collector.util;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;

public class BlackDuckResponseCache {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckResponseCache.class);
    private BlackDuckBucketService blackDuckBucketService;
    private BlackDuckBucket bucket;
    private long timeout;

    public BlackDuckResponseCache(final BlackDuckBucketService blackDuckBucketService, final BlackDuckBucket bucket, final long timeout) {
        this.blackDuckBucketService = blackDuckBucketService;
        this.bucket = bucket;
        this.timeout = timeout;
    }

    public <T extends BlackDuckResponse> Optional<T> getItem(Class<T> responseClass, String url) {
        try {
            Future<Optional<T>> optionalProjectVersionFuture = blackDuckBucketService.addToTheBucket(bucket, url, responseClass);
            return optionalProjectVersionFuture
                       .get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            logger.debug("The thread was interrupted, failing safely...");
            Thread.currentThread().interrupt();
        } catch (Exception genericException) {
            logger.error("There was a problem retrieving the Project Version link.", genericException);
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
                   .flatMap(view -> view.getFirstLink(link));
    }

    public Optional<VersionBomComponentView> getBomComponentView(String bomComponentUrl) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(bomComponentUrl)) {
            return getItem(VersionBomComponentView.class, bomComponentUrl);
        }
        return Optional.empty();
    }

    public String getSeverity(String vulnerabilityUrl) {
        String severity = "UNKNOWN";
        try {
            Optional<VulnerabilityView> vulnerabilityView = getItem(VulnerabilityView.class, vulnerabilityUrl);
            if (vulnerabilityView.isPresent()) {
                VulnerabilityView vulnerability = vulnerabilityView.get();
                severity = vulnerability.getSeverity();
                Optional<String> cvss3Severity = getCvss3Severity(vulnerability);
                if (cvss3Severity.isPresent()) {
                    severity = cvss3Severity.get();
                }
            }
        } catch (Exception e) {
            logger.debug("Error fetching vulnerability view", e);
        }

        return severity;
    }

    // TODO update this code with an Object from blackduck-common-api when available
    private Optional<String> getCvss3Severity(VulnerabilityView vulnerabilityView) {
        Boolean useCvss3 = vulnerabilityView.getUseCvss3();
        if (null != useCvss3 && useCvss3) {
            JsonObject vulnJsonObject = vulnerabilityView.getJsonElement().getAsJsonObject();
            JsonElement cvss3 = vulnJsonObject.get("cvss3");
            if (null != cvss3) {
                JsonElement cvss3Severity = cvss3.getAsJsonObject().get("severity");
                if (null != cvss3Severity) {
                    return Optional.of(cvss3Severity.getAsString());
                }
            }
        }
        return Optional.empty();
    }

}
