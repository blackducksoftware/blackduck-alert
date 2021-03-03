/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.collector.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class AlertBlackDuckService {
    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(getClass()));
    private final BlackDuckApiClient blackDuckApiClient;

    public AlertBlackDuckService(BlackDuckApiClient blackDuckApiClient) {
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public Optional<ComponentVersionView> getComponentVersion(ProjectVersionComponentView versionBomComponent) {
        try {
            return Optional.of(blackDuckApiClient.getResponse(new HttpUrl(versionBomComponent.getComponentVersion()), ComponentVersionView.class));
        } catch (IntegrationException e) {
            logger.errorAndDebug("Could not retrieve the Component Version: " + e.getMessage(), e);
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
            logger.errorAndDebug("Could not retrieve the Project link: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<ProjectVersionComponentView> getBomComponentView(String bomComponentUrl) {
        try {
            if (StringUtils.isNotBlank(bomComponentUrl)) {
                return Optional.of(blackDuckApiClient.getResponse(new HttpUrl(bomComponentUrl), ProjectVersionComponentView.class));
            }
        } catch (IntegrationException e) {
            logger.errorAndDebug("Could not retrieve the Bom component: " + e.getMessage(), e);
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
            logger.debug(String.format("Unable to get policy rule: %s", policyInfo.getPolicyName()), e);
        }
        return Optional.empty();
    }

    public Optional<ProjectVersionWrapper> getProjectVersionWrapper(String projectVersionUrl) {
        try {
            ProjectVersionView projectVersionView = blackDuckApiClient.getResponse(new HttpUrl(projectVersionUrl), ProjectVersionView.class);
            ProjectVersionWrapper wrapper = new ProjectVersionWrapper();
            wrapper.setProjectVersionView(projectVersionView);

            ProjectView projectView = blackDuckApiClient.getResponse(projectVersionView.getFirstLink(ProjectVersionView.PROJECT_LINK), ProjectView.class);
            wrapper.setProjectView(projectView);
            return Optional.of(wrapper);
        } catch (IntegrationException e) {
            logger.errorAndDebug("Could not retrieve the Project Version: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

}
