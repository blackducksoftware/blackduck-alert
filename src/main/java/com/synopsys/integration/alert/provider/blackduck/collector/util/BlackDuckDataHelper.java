/**
 * blackduck-alert
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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.component.RemediatingVersionView;
import com.synopsys.integration.blackduck.api.generated.response.RemediationOptionsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

public class BlackDuckDataHelper {
    public static final String VULNERABILITY_CHECK_TEXT = "vuln";
    private final Logger logger = LoggerFactory.getLogger(BlackDuckDataHelper.class);
    private BlackDuckProperties blackDuckProperties;
    private BlackDuckService blackDuckService;
    private BlackDuckBucket blackDuckBucket;
    private BlackDuckBucketService bucketService;

    public BlackDuckDataHelper(BlackDuckProperties blackDuckProperties, BlackDuckService blackDuckService, BlackDuckBucket blackDuckBucket, BlackDuckBucketService bucketService) {
        this.blackDuckProperties = blackDuckProperties;
        this.blackDuckService = blackDuckService;
        this.blackDuckBucket = blackDuckBucket;
        this.bucketService = bucketService;
    }

    public Optional<String> getProjectComponentQueryLink(String projectVersionUrl, String link, String componentName) {
        Optional<String> projectLink = getProjectLink(projectVersionUrl, link);
        return projectLink.flatMap(optionalProjectLink -> getProjectComponentQueryLink(optionalProjectLink, componentName));
    }

    public Optional<String> getProjectComponentQueryLink(String projectLink, String componentName) {
        return Optional.of(String.format("%s?q=componentName:%s", projectLink, componentName));
    }

    public Optional<String> getProjectLink(String projectVersionUrl, String link) {
        try {
            Future<Optional<ProjectVersionView>> optionalProjectVersionFuture = bucketService.addToTheBucket(blackDuckBucket, projectVersionUrl, ProjectVersionView.class);
            return optionalProjectVersionFuture
                       .get(blackDuckProperties.getBlackDuckTimeout(), TimeUnit.SECONDS)
                       .flatMap(view -> view.getFirstLink(link));
        } catch (InterruptedException interruptedException) {
            logger.debug("The thread was interrupted, failing safely...");
            Thread.currentThread().interrupt();
        } catch (Exception genericException) {
            logger.error("There was a problem retrieving the Project Version link.", genericException);
        }

        return Optional.empty();
    }

    public Optional<ProjectVersionWrapper> getProjectVersionWrapper(VersionBomComponentView versionBomComponent) {
        try {
            // TODO Stop using this when Black Duck supports going back to the project-version
            final Optional<String> versionBomComponentHref = versionBomComponent.getHref();
            if (versionBomComponentHref.isPresent()) {
                String versionHref = versionBomComponentHref.get();
                int componentsIndex = versionHref.indexOf(ProjectVersionView.COMPONENTS_LINK);
                String projectVersionUri = versionHref.substring(0, componentsIndex - 1);

                bucketService.addToTheBucket(blackDuckBucket, projectVersionUri, ProjectVersionView.class);
                ProjectVersionView projectVersion = blackDuckBucket.get(projectVersionUri, ProjectVersionView.class);
                ProjectVersionWrapper wrapper = new ProjectVersionWrapper();
                wrapper.setProjectVersionView(projectVersion);
                blackDuckService.getResponse(projectVersion, ProjectVersionView.PROJECT_LINK_RESPONSE).ifPresent(wrapper::setProjectView);
                return Optional.of(wrapper);
            }
        } catch (IntegrationException ie) {
            logger.error("Error getting project version for Bom Component. ", ie);
        }

        return Optional.empty();
    }

    public Optional<VersionBomComponentView> getBomComponentView(String bomComponentUrl) {
        try {
            if (StringUtils.isNotBlank(bomComponentUrl)) {
                Future<Optional<VersionBomComponentView>> optionalVersionBomComponentFuture = bucketService.addToTheBucket(blackDuckBucket, bomComponentUrl, VersionBomComponentView.class);
                return optionalVersionBomComponentFuture.get(blackDuckProperties.getBlackDuckTimeout(), TimeUnit.SECONDS);
            }
        } catch (InterruptedException interruptedException) {
            logger.debug("The thread was interrupted, failing safely...");
            Thread.currentThread().interrupt();
        } catch (Exception genericException) {
            logger.error("Error retrieving bom component", genericException);
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

    public List<VersionBomPolicyRuleView> getPolicyRulesFromComponent(VersionBomComponentView bomComponentView) {
        try {
            return blackDuckService.getAllResponses(bomComponentView, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
        } catch (IntegrationException e) {
            logger.debug("Unable to get policy rules from component: {}[{}]", bomComponentView.getComponentName(), bomComponentView.getComponentVersionName());
        }
        return List.of();
    }

    public List<VulnerabilityView> getVulnerabilitiesForComponent(VulnerableComponentView vulnerableComponentView) {
        try {
            return blackDuckService.getAllResponses(vulnerableComponentView, VulnerableComponentView.VULNERABILITIES_LINK_RESPONSE);
        } catch (IntegrationException ex) {
            logger.error("Error getting vulnerabilities ", ex);
        }
        return List.of();
    }

    public List<VulnerableComponentView> getVulnerableComponentViews(ProjectVersionWrapper projectVersionWrapper, VersionBomComponentView versionBomComponent) throws IntegrationException {
        return blackDuckService.getAllResponses(projectVersionWrapper.getProjectVersionView(), ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE).stream()
                   .filter(vulnerableComponentView -> vulnerableComponentView.getComponentName().equals(versionBomComponent.getComponentName()))
                   .filter(vulnerableComponentView -> vulnerableComponentView.getComponentVersionName().equals(versionBomComponent.getComponentVersionName()))
                   .collect(Collectors.toList());
    }

    public boolean hasVulnerabilityRule(VersionBomPolicyRuleView policyRule) {
        PolicyRuleExpressionSetView expression = policyRule.getExpression();
        List<PolicyRuleExpressionView> expressions = expression.getExpressions();
        for (PolicyRuleExpressionView expressionView : expressions) {
            if (expressionView.getName().toLowerCase().contains(VULNERABILITY_CHECK_TEXT)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasVulnerabilityRule(PolicyRuleView policyRule) {
        PolicyRuleExpressionSetView expression = policyRule.getExpression();
        List<PolicyRuleExpressionView> expressions = expression.getExpressions();
        for (PolicyRuleExpressionView expressionView : expressions) {
            if (expressionView.getName().toLowerCase().contains(VULNERABILITY_CHECK_TEXT)) {
                return true;
            }
        }
        return false;
    }

    public Optional<PolicyRuleView> getPolicyRule(PolicyInfo policyInfo) {
        try {
            String policyUrl = policyInfo.getPolicy();
            if (StringUtils.isNotBlank(policyUrl)) {
                bucketService.addToTheBucket(blackDuckBucket, policyUrl, PolicyRuleView.class);
                return Optional.of(blackDuckBucket.get(policyUrl, PolicyRuleView.class));
            }
        } catch (Exception e) {
            logger.debug("Unable to get policy rule: {}", policyInfo.getPolicyName());
            logger.debug("Cause:", e);
        }
        return Optional.empty();
    }

    public List<LinkableItem> getRemediationItems(ComponentVersionView componentVersionView) throws IntegrationException {
        List<LinkableItem> remediationItems = new LinkedList<>();
        ComponentService componentService = new ComponentService(blackDuckService, new Slf4jIntLogger(logger));
        Optional<RemediationOptionsView> optionalRemediation = componentService.getRemediationInformation(componentVersionView);
        if (optionalRemediation.isPresent()) {
            RemediationOptionsView remediationOptions = optionalRemediation.get();
            createRemediationItem(remediationOptions::getFixesPreviousVulnerabilities, BlackDuckContent.LABEL_REMEDIATION_FIX_PREVIOUS).ifPresent(remediationItems::add);
            createRemediationItem(remediationOptions::getLatestAfterCurrent, BlackDuckContent.LABEL_REMEDIATION_LATEST).ifPresent(remediationItems::add);
            createRemediationItem(remediationOptions::getNoVulnerabilities, BlackDuckContent.LABEL_REMEDIATION_CLEAN).ifPresent(remediationItems::add);
        }
        return remediationItems;
    }

    private Optional<LinkableItem> createRemediationItem(Supplier<RemediatingVersionView> getRemediationOption, String remediationLabel) {
        RemediatingVersionView remediatingVersionView = getRemediationOption.get();
        if (null != remediatingVersionView) {
            String versionText = createRemediationVersionText(remediatingVersionView);
            return Optional.of(new LinkableItem(remediationLabel, versionText, remediatingVersionView.getComponentVersion()));
        }
        return Optional.empty();
    }

    private String createRemediationVersionText(RemediatingVersionView remediatingVersionView) {
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
