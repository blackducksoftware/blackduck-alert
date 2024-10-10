/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.validator;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.blackduck.api.core.BlackDuckResponse;
import com.blackduck.integration.blackduck.api.core.ResourceLink;
import com.blackduck.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.http.client.ApiTokenBlackDuckHttpClient;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.response.Response;

public class BlackDuckApiTokenValidator {
    public static final String ROLE_NAME_GLOBAL_PROJECT_VIEWER = "Global Project Viewer";
    public static final String ROLE_NAME_SYSTEM_ADMINISTRATOR = "System Administrator";
    // As of Blackduck 2022.7.0 Super User has been removed and replaced with a combination of the roles below. See IALERT-3105 for more info.
    public static final String ROLE_NAME_SUPER_USER = "Super User";
    public static final String ROLE_NAME_GLOBAL_PROJECT_ADMINISTRATOR = "Global Project Administrator";
    public static final String ROLE_NAME_GLOBAL_GROUP_ADMINISTRATOR = "Global Project Group Administrator";

    // FIXME use these when RoleAssignmentView is deserialized correctly (requires blackduck-common-api support)
    public static final String ROLE_KEY_GLOBAL_PROJECT_VIEWER = "globalprojectviewer";
    public static final String ROLE_KEY_SYSTEM_ADMINISTRATOR = "sysadmin";
    public static final String ROLE_KEY_SUPER_USER = "superuser";

    private static final List<String> PERMITTED_ROLE_NAMES = List.of(
        ROLE_NAME_GLOBAL_PROJECT_VIEWER,
        ROLE_NAME_SYSTEM_ADMINISTRATOR,
        ROLE_NAME_SUPER_USER,
        ROLE_NAME_GLOBAL_PROJECT_ADMINISTRATOR,
        ROLE_NAME_GLOBAL_GROUP_ADMINISTRATOR
    );

    private final BlackDuckProperties blackDuckProperties;
    private final Logger logger = LoggerFactory.getLogger(BlackDuckApiTokenValidator.class);

    public BlackDuckApiTokenValidator(BlackDuckProperties blackDuckProperties) {
        this.blackDuckProperties = blackDuckProperties;
    }

    public Response attemptAuthentication() throws IntegrationException {
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        ApiTokenBlackDuckHttpClient apiTokenBlackDuckHttpClient = blackDuckProperties.createApiTokenBlackDuckHttpClient(intLogger);
        return apiTokenBlackDuckHttpClient.attemptAuthentication();
    }

    public boolean isApiTokenValid() {
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        return blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger)
            .map(httpClient -> blackDuckProperties.createBlackDuckServicesFactory(httpClient, intLogger))
            .map(this::validateToken)
            .orElse(false);
    }

    private boolean validateToken(BlackDuckServicesFactory blackDuckServicesFactory) {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        Optional<UserView> currentUser = getCurrentUser(blackDuckServicesFactory);
        return currentUser
            .map(user -> hasPermittedRole(blackDuckApiClient, user) || hasWatchedProjects(blackDuckApiClient, user))
            .orElse(false);

    }

    private Optional<UserView> getCurrentUser(BlackDuckServicesFactory blackDuckServicesFactory) {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();

        UserView currentUser = null;
        try {
            currentUser = blackDuckApiClient.getResponse(apiDiscovery.metaCurrentUserLink());
        } catch (IntegrationException integrationException) {
            logger.error("Failed to GET the currently authenticated Black Duck user", integrationException);
        }
        return Optional.ofNullable(currentUser);
    }

    private boolean hasPermittedRole(BlackDuckApiClient blackDuckApiClient, UserView currentUser) {
        try {
            Predicate<RoleAssignmentView> predicate = role -> PERMITTED_ROLE_NAMES.contains(role.getName());
            return !blackDuckApiClient.getSomeMatchingResponses(currentUser.metaInheritedRolesLink(), predicate, 1).isEmpty();
        } catch (IntegrationException integrationException) {
            logger.error("Failed to GET the currently authenticated Black Duck user's roles", integrationException);
        }
        return false;
    }

    private boolean hasWatchedProjects(BlackDuckApiClient blackDuckApiClient, UserView currentUser) {
        try {
            Optional<UrlMultipleResponses<AlertNotificationSubscriptionsSubscriptionView>> notificationResponses = currentUser.getMeta().getLinks()
                .stream()
                .filter(resourceLink -> resourceLink.getRel().equals("notification-subscriptions"))
                .map(ResourceLink::getHref)
                .map(url -> new UrlMultipleResponses<>(url, AlertNotificationSubscriptionsSubscriptionView.class))
                .findFirst();
            Predicate<AlertNotificationSubscriptionsSubscriptionView> predicate = AlertNotificationSubscriptionsSubscriptionView::getNotifyUser;
            if (notificationResponses.isPresent()) {
                return !blackDuckApiClient.getSomeMatchingResponses(notificationResponses.get(), predicate, 1).isEmpty();
            }
        } catch (IntegrationException integrationException) {
            logger.error("Failed to GET the currently authenticated Black Duck user's roles", integrationException);
        }
        return false;
    }

    // The API generator create NotificationSubscriptionsSubscriptionView but it is a BlackDuckComponent not a response.
    // The API generator would need to be run against the latest
    public static class AlertNotificationSubscriptionsSubscriptionView extends BlackDuckResponse {
        private Date createdAt;
        private Boolean notifyUser;
        private String subscriptionTarget;
        private String subscriptionTargetProjectName;
        private String subscriptionTargetReleaseName;

        public AlertNotificationSubscriptionsSubscriptionView() {
        }

        public Date getCreatedAt() {
            return this.createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public Boolean getNotifyUser() {
            return this.notifyUser;
        }

        public void setNotifyUser(Boolean notifyUser) {
            this.notifyUser = notifyUser;
        }

        public String getSubscriptionTarget() {
            return this.subscriptionTarget;
        }

        public void setSubscriptionTarget(String subscriptionTarget) {
            this.subscriptionTarget = subscriptionTarget;
        }

        public String getSubscriptionTargetProjectName() {
            return this.subscriptionTargetProjectName;
        }

        public void setSubscriptionTargetProjectName(String subscriptionTargetProjectName) {
            this.subscriptionTargetProjectName = subscriptionTargetProjectName;
        }

        public String getSubscriptionTargetReleaseName() {
            return this.subscriptionTargetReleaseName;
        }

        public void setSubscriptionTargetReleaseName(String subscriptionTargetReleaseName) {
            this.subscriptionTargetReleaseName = subscriptionTargetReleaseName;
        }
    }
}
