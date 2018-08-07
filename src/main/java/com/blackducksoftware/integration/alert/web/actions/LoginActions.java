/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.alert.web.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.model.LoginConfig;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.RoleAssignmentView;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.UserGroupService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.FieldEnum;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResults;

@Component
public class LoginActions {

    private final BlackDuckProperties blackDuckProperties;

    @Autowired
    public LoginActions(final BlackDuckProperties blackDuckProperties) {
        this.blackDuckProperties = blackDuckProperties;
    }

    public boolean authenticateUser(final LoginConfig loginConfig, final IntLogger logger) throws IntegrationException {
        final HubServerConfigBuilder serverConfigBuilder = blackDuckProperties.createServerConfigBuilderWithoutAuthentication(logger, HubServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS);

        serverConfigBuilder.setPassword(loginConfig.getBlackDuckPassword());
        serverConfigBuilder.setUsername(loginConfig.getBlackDuckUsername());

        try {
            validateBlackDuckConfiguration(serverConfigBuilder);
            try (final RestConnection restConnection = createRestConnection(serverConfigBuilder)) {
                restConnection.connect();
                logger.info("Connected");
                final boolean isValidLoginUser = isUserRoleValid(loginConfig.getBlackDuckUsername(), restConnection);
                if (isValidLoginUser) {
                    final Authentication authentication = new UsernamePasswordAuthenticationToken(loginConfig.getBlackDuckUsername(), loginConfig.getBlackDuckPassword(), Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return authentication.isAuthenticated();
                }
            } catch (final IOException ex) {
                logger.error("Rest connection close failure", ex);
            }
        } catch (final AlertFieldException afex) {
            logger.error("Error establishing connection", afex);
            final Map<String, String> fieldErrorMap = afex.getFieldErrors();
            fieldErrorMap.keySet().forEach(key -> {
                final String value = fieldErrorMap.get(key);
                logger.error(String.format("Field Error %s - %s", key, value));
            });

            logger.info("User not authenticated");
            return false;
        } catch (final IntegrationException ex) {
            logger.error("Error establishing connection", ex);
            logger.info("User not authenticated");
            return false;
        }

        logger.info("User role not authenticated");
        return false;
    }

    public boolean isUserRoleValid(final String userName, final RestConnection restConnection) {
        final HubServicesFactory blackDuckServicesFactory = new HubServicesFactory(restConnection);
        final UserGroupService userGroupService = blackDuckServicesFactory.createUserGroupService();

        try {
            final List<RoleAssignmentView> userRoles = userGroupService.getRolesForUser(userName);
            for (final RoleAssignmentView roles : userRoles) {
                if ("System Administrator".equalsIgnoreCase(roles.name)) {
                    return true;
                }
            }
        } catch (final IntegrationException e) {
            return false;
        }

        return false;
    }

    public void validateBlackDuckConfiguration(final HubServerConfigBuilder blackDuckServerConfigBuilder) throws AlertFieldException {
        final AbstractValidator validator = blackDuckServerConfigBuilder.createValidator();
        final ValidationResults results = validator.assertValid();
        if (!results.getResultMap().isEmpty()) {
            final Map<String, String> fieldErrors = new HashMap<>();
            for (final Entry<FieldEnum, Set<ValidationResult>> result : results.getResultMap().entrySet()) {
                final Set<ValidationResult> validationResult = result.getValue();
                final List<String> errors = new ArrayList<>();
                for (final ValidationResult currentValidationResult : validationResult) {
                    errors.add(currentValidationResult.getMessage());
                }

                final String key = result.getKey().getKey();
                final String errorMessage = StringUtils.join(errors, " , ");
                fieldErrors.put(key, errorMessage);
            }
            throw new AlertFieldException("There were issues with the configuration.", fieldErrors);
        }
    }

    public RestConnection createRestConnection(final HubServerConfigBuilder blackDuckServerConfigBuilder) throws IntegrationException {
        final HubServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        return blackDuckServerConfig.createCredentialsRestConnection(blackDuckServerConfigBuilder.getLogger());
    }
}
