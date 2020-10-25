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

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VulnerabilityNotificationView;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;

public class BlackDuckProjectNameExtractor {
    private static final BiFunction<NotificationDeserializationCache, AlertNotificationModel, Collection<String>> DEFAULT_EXTRACTOR = (x, y) -> List.of();

    private final Logger logger = LoggerFactory.getLogger(BlackDuckProjectNameExtractor.class);
    private Map<String, BiFunction<NotificationDeserializationCache, AlertNotificationModel, Collection<String>>> extractorMap = new HashMap<>();
    private BlackDuckResponseCache blackDuckResponseCache;

    public BlackDuckProjectNameExtractor(BlackDuckBucketService blackDuckBucketService, int timeout) {
        this.blackDuckResponseCache = new BlackDuckResponseCache(blackDuckBucketService, new BlackDuckBucket(), timeout);
        initializeExtractorMap();
    }

    public Collection<String> getProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        return extractorMap
                   .getOrDefault(notification.getNotificationType(), DEFAULT_EXTRACTOR)
                   .apply(cache, notification);
    }

    private Collection<String> getBomEditProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        BomEditNotificationView notificationView = cache.getTypedContent(notification, BomEditNotificationView.class);
        String bomComponentUri = notificationView.getContent().getBomComponent();
        Optional<ProjectVersionComponentView> bomComponentView = blackDuckResponseCache.getItem(ProjectVersionComponentView.class, bomComponentUri);
        if (bomComponentView.isPresent()) {
            return getProjectName(blackDuckResponseCache, bomComponentView.get())
                       .stream()
                       .collect(Collectors.toSet());
        }
        logger.warn("Could not get project name for Bom Edit notification");
        return Set.of();
    }

    private Collection<String> getPolicyOverrideProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        PolicyOverrideNotificationView notificationView = cache.getTypedContent(notification, PolicyOverrideNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getProjectNotificationProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        ProjectNotificationView notificationView = cache.getTypedContent(notification, ProjectNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getProjectVersionNotificationProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        ProjectVersionNotificationView notificationView = cache.getTypedContent(notification, ProjectVersionNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getRuleViolationProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        RuleViolationNotificationView notificationView = cache.getTypedContent(notification, RuleViolationNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getRuleViolationClearedProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        RuleViolationClearedNotificationView notificationView = cache.getTypedContent(notification, RuleViolationClearedNotificationView.class);
        return Set.of(notificationView.getContent().getProjectName());
    }

    private Collection<String> getVulnerabilityProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        VulnerabilityNotificationView notificationView = cache.getTypedContent(notification, VulnerabilityNotificationView.class);
        return notificationView.getContent().getAffectedProjectVersions()
                   .stream()
                   .map(AffectedProjectVersion::getProjectName)
                   .collect(Collectors.toSet());
    }

    private Optional<String> getProjectName(BlackDuckResponseCache blackDuckResponseCache, ProjectVersionComponentView versionBomComponent) {
        Optional<String> versionBomComponentHref = versionBomComponent.getHref();
        if (versionBomComponentHref.isPresent()) {
            String versionHref = versionBomComponentHref.get();
            int projectVersionIndex = versionHref.indexOf(ProjectView.VERSIONS_LINK);
            String projectUri = versionHref.substring(0, projectVersionIndex - 1);

            Optional<ProjectView> projectView = blackDuckResponseCache.getItem(ProjectView.class, projectUri);
            return projectView.map(ProjectView::getName);
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
