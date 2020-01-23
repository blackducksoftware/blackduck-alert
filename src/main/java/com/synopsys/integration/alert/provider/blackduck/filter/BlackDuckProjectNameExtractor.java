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
package com.synopsys.integration.alert.provider.blackduck.filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectViewV4;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.manual.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VulnerabilityNotificationView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class BlackDuckProjectNameExtractor {
    private static final BiFunction<NotificationDeserializationCache, AlertNotificationWrapper, Collection<String>> DEFAULT_EXTRACTOR = (x, y) -> List.of();

    private final Logger logger = LoggerFactory.getLogger(BlackDuckProjectNameExtractor.class);
    private Map<String, BiFunction<NotificationDeserializationCache, AlertNotificationWrapper, Collection<String>>> extractorMap = new HashMap<>();
    private BlackDuckProperties blackDuckProperties;

    @Autowired
    public BlackDuckProjectNameExtractor(BlackDuckProperties blackDuckProperties) {
        this.blackDuckProperties = blackDuckProperties;
        initializeExtractorMap();
    }

    public Collection<String> getProjectNames(NotificationDeserializationCache cache, AlertNotificationWrapper notification) {
        return extractorMap
                   .getOrDefault(notification.getNotificationType(), DEFAULT_EXTRACTOR)
                   .apply(cache, notification);
    }

    private Collection<String> getBomEditProjectNames(NotificationDeserializationCache cache, AlertNotificationWrapper notification) {
        Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger);
        if (optionalBlackDuckHttpClient.isPresent()) {
            BlackDuckHttpClient blackDuckHttpClient = optionalBlackDuckHttpClient.get();
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, blackDuckHttpClient.getLogger());
            BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();

            BomEditNotificationView notificationView = cache.getTypedContent(notification, BomEditNotificationView.class);
            String bomComponentUri = notificationView.getContent().getBomComponent();
            try {
                VersionBomComponentView bomComponentView = blackDuckService.getResponse(bomComponentUri, VersionBomComponentView.class);
                return getProjectName(blackDuckService, bomComponentView)
                           .stream()
                           .collect(Collectors.toSet());
            } catch (IntegrationException e) {
                logger.error("An exception occurred while trying to get the Bom Component View", e);
            }
        }
        logger.warn("Could not get project name for Bom Edit notification");
        return Set.of();
    }

    private Collection<String> getPolicyOverrideProjectNames(NotificationDeserializationCache cache, AlertNotificationWrapper notification) {
        PolicyOverrideNotificationView notificationView = cache.getTypedContent(notification, PolicyOverrideNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getProjectNotificationProjectNames(NotificationDeserializationCache cache, AlertNotificationWrapper notification) {
        ProjectNotificationView notificationView = cache.getTypedContent(notification, ProjectNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getProjectVersionNotificationProjectNames(NotificationDeserializationCache cache, AlertNotificationWrapper notification) {
        ProjectVersionNotificationView notificationView = cache.getTypedContent(notification, ProjectVersionNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getRuleViolationProjectNames(NotificationDeserializationCache cache, AlertNotificationWrapper notification) {
        RuleViolationNotificationView notificationView = cache.getTypedContent(notification, RuleViolationNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getRuleViolationClearedProjectNames(NotificationDeserializationCache cache, AlertNotificationWrapper notification) {
        RuleViolationClearedNotificationView notificationView = cache.getTypedContent(notification, RuleViolationClearedNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getVulnerabilityProjectNames(NotificationDeserializationCache cache, AlertNotificationWrapper notification) {
        VulnerabilityNotificationView notificationView = cache.getTypedContent(notification, VulnerabilityNotificationView.class);
        return notificationView.getContent().getAffectedProjectVersions()
                   .stream()
                   .map(AffectedProjectVersion::getProjectName)
                   .collect(Collectors.toSet());
    }

    private Optional<String> getProjectName(BlackDuckService blackDuckService, VersionBomComponentView versionBomComponent) {
        try {
            Optional<String> versionBomComponentHref = versionBomComponent.getHref();
            if (versionBomComponentHref.isPresent()) {
                String versionHref = versionBomComponentHref.get();
                int projectVersionIndex = versionHref.indexOf(ProjectViewV4.VERSIONS_LINK);
                String projectUri = versionHref.substring(0, projectVersionIndex - 1);

                ProjectView projectView = blackDuckService.getResponse(projectUri, ProjectView.class);
                return Optional.of(projectView.getName());
            }
        } catch (IntegrationException ie) {
            logger.error("Error getting project version for Bom Component. ", ie);
        }

        return Optional.empty();
    }

    private void initializeExtractorMap() {
        extractorMap.put(NotificationType.BOM_EDIT.name(), this::getBomEditProjectNames);
        extractorMap.put(NotificationType.LICENSE_LIMIT.name(), DEFAULT_EXTRACTOR);
        extractorMap.put(NotificationType.POLICY_OVERRIDE.name(), this::getPolicyOverrideProjectNames);
        extractorMap.put(NotificationType.PROJECT.name(), this::getProjectNotificationProjectNames);
        extractorMap.put(NotificationType.PROJECT_VERSION.name(), this::getProjectVersionNotificationProjectNames);
        extractorMap.put(NotificationType.RULE_VIOLATION.name(), this::getRuleViolationProjectNames);
        extractorMap.put(NotificationType.RULE_VIOLATION_CLEARED.name(), this::getRuleViolationClearedProjectNames);
        extractorMap.put(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name(), DEFAULT_EXTRACTOR);
        extractorMap.put(NotificationType.VULNERABILITY.name(), this::getVulnerabilityProjectNames);
    }

}
