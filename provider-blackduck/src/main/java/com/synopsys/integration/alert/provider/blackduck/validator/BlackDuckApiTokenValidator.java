/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.validator;

import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.http.client.ApiTokenBlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckApiTokenValidator {
    public static final String ROLE_NAME_GLOBAL_PROJECT_VIEWER = "Global Project Viewer";
    public static final String ROLE_NAME_SYSTEM_ADMINISTRATOR = "System Administrator";
    public static final String ROLE_NAME_SUPER_USER = "Super User";

    // FIXME use these when RoleAssignmentView is deserialized correctly (requires blackduck-common-api support)
    public static final String ROLE_KEY_GLOBAL_PROJECT_VIEWER = "globalprojectviewer";
    public static final String ROLE_KEY_SYSTEM_ADMINISTRATOR = "sysadmin";
    public static final String ROLE_KEY_SUPER_USER = "superuser";

    private static final List<String> PERMITTED_ROLE_NAMES = List.of(
        ROLE_NAME_GLOBAL_PROJECT_VIEWER,
        ROLE_NAME_SYSTEM_ADMINISTRATOR,
        ROLE_NAME_SUPER_USER
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
            .map(this::hasPermittedRole)
            .orElse(false);
    }

    private boolean hasPermittedRole(BlackDuckServicesFactory blackDuckServicesFactory) {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();

        UserView currentUser;
        try {
            currentUser = blackDuckApiClient.getResponse(apiDiscovery.metaCurrentUserLink());
        } catch (IntegrationException integrationException) {
            logger.error("Failed to GET the currently authenticated Black Duck user", integrationException);
            return false;
        }

        try {
            Predicate<RoleAssignmentView> predicate = role -> PERMITTED_ROLE_NAMES.contains(role.getName());
            return !blackDuckApiClient.getSomeMatchingResponses(currentUser.metaInheritedRolesLink(), predicate, 1).isEmpty();
        } catch (IntegrationException integrationException) {
            logger.error("Failed to GET the currently authenticated Black Duck user's roles", integrationException);
        }
        return false;
    }

}
