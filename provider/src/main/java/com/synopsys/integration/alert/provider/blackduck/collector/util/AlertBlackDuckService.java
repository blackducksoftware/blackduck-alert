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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class AlertBlackDuckService {
    private final Logger logger = LoggerFactory.getLogger(AlertBlackDuckService.class);
    private final BlackDuckApiClient blackDuckApiClient;

    public AlertBlackDuckService(BlackDuckApiClient blackDuckApiClient) {
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public Optional<ComponentVersionView> getComponentVersion(ProjectVersionComponentView versionBomComponent) {
        try {
            return Optional.of(blackDuckApiClient.getResponse(new HttpUrl(versionBomComponent.getComponentVersion()), ComponentVersionView.class));
        } catch (IntegrationException e) {
            logger.error("Could not retrieve the Component Version: ", e.getMessage());
            logger.debug(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<String> getProjectComponentQueryLink(String projectVersionUrl, String link, String componentName) {
        Optional<String> projectLinkOptional = getProjectLink(projectVersionUrl, link);
        return projectLinkOptional.map(projectLink -> getProjectComponentQueryLink(projectLink, componentName));
    }

    public String getProjectComponentQueryLink(String projectLink, String componentName) {
        return String.format("%s?q=componentName:%s", projectLink, componentName);
    }

    public Optional<String> getProjectLink(String projectVersionUrl, String link) {
        try {
            ProjectVersionView projectVersionView = blackDuckApiClient.getResponse(new HttpUrl(projectVersionUrl), ProjectVersionView.class);
            return Optional.of(projectVersionView.getFirstLink(link).toString());
        } catch (IntegrationException e) {
            logger.error("Could not retrieve the Project link: ", e.getMessage());
            logger.debug(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<ProjectVersionComponentView> getBomComponentView(String bomComponentUrl) {
        try {
            if (StringUtils.isNotBlank(bomComponentUrl)) {
                return Optional.of(blackDuckApiClient.getResponse(new HttpUrl(bomComponentUrl), ProjectVersionComponentView.class));
            }
        } catch (IntegrationException e) {
            logger.error("Could not retrieve the Bom component: ", e.getMessage());
            logger.debug(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<PolicyRuleView> getPolicyRule(PolicyInfo policyInfo) {
        try {
            String policyUrl = policyInfo.getPolicy();
            if (StringUtils.isNotBlank(policyUrl)) {
                return Optional.of(blackDuckApiClient.getResponse(new HttpUrl(policyUrl), PolicyRuleView.class));
            }
        } catch (IntegrationException e) {
            logger.debug("Unable to get policy rule: {}", policyInfo.getPolicyName());
            logger.debug("Cause:", e);
        }
        return Optional.empty();
    }

    public Optional<ProjectVersionWrapper> getProjectVersionWrapper(String projectVersionUrl) {
        try {
            ProjectVersionView projectVersionView = blackDuckApiClient.getResponse(new HttpUrl(projectVersionUrl), ProjectVersionView.class);
            ProjectVersionWrapper wrapper = new ProjectVersionWrapper();
            wrapper.setProjectVersionView(projectVersionView);

            ProjectView projectView = blackDuckApiClient.getResponse(new HttpUrl(projectVersionView.getFirstLink(ProjectVersionView.PROJECT_LINK).toString()), ProjectView.class);
            wrapper.setProjectView(projectView);
            return Optional.of(wrapper);
        } catch (IntegrationException e) {
            logger.error("Could not retrieve the Project Version: ", e.getMessage());
            logger.debug(e.getMessage(), e);
        }
        return Optional.empty();
    }

}
