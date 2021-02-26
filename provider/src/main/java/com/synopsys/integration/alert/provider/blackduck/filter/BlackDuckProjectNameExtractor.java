/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
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
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckProjectNameExtractor {
    private static final BiFunction<NotificationDeserializationCache, AlertNotificationModel, Collection<String>> DEFAULT_EXTRACTOR = (x, y) -> List.of();

    private final Logger logger = LoggerFactory.getLogger(BlackDuckProjectNameExtractor.class);
    private final Map<String, BiFunction<NotificationDeserializationCache, AlertNotificationModel, Collection<String>>> extractorMap = new HashMap<>();
    private final BlackDuckServicesFactory blackDuckServicesFactory;

    public BlackDuckProjectNameExtractor(BlackDuckServicesFactory blackDuckServicesFactory) {
        this.blackDuckServicesFactory = blackDuckServicesFactory;
        initializeExtractorMap();
    }

    public Collection<String> getProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        return extractorMap
                   .getOrDefault(notification.getNotificationType(), DEFAULT_EXTRACTOR)
                   .apply(cache, notification);
    }

    private Collection<String> getBomEditProjectNames(NotificationDeserializationCache cache, AlertNotificationModel notification) {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();

        BomEditNotificationView notificationView = cache.getTypedContent(notification, BomEditNotificationView.class);
        String bomComponentUri = notificationView.getContent().getBomComponent();
        try {
            HttpUrl bomComponentUrl = new HttpUrl(bomComponentUri);
            ProjectVersionComponentView bomComponentView = blackDuckApiClient.getResponse(bomComponentUrl, ProjectVersionComponentView.class);
            return getProjectName(blackDuckApiClient, bomComponentView)
                       .stream()
                       .collect(Collectors.toSet());
        } catch (IntegrationException e) {
            logger.error("An exception occurred while trying to get the Bom Component View", e);
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

    private Optional<String> getProjectName(BlackDuckApiClient blackDuckApiClient, ProjectVersionComponentView versionBomComponent) {
        try {
            String bomComponentHref = versionBomComponent.getHref().toString();
            String projectUri = StringUtils.substringBefore(bomComponentHref, "/" + ProjectView.VERSIONS_LINK);

            HttpUrl projectHttpUrl = new HttpUrl(projectUri);
            ProjectView projectView = blackDuckApiClient.getResponse(projectHttpUrl, ProjectView.class);
            return Optional.of(projectView.getName());
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
