/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.user.UserDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.view.RoleView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.FieldEnum;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResults;

@Component
public class LoginActions {

    public boolean authenticateUser(final LoginRestModel loginRestModel, final IntLogger logger) throws IntegrationException {
        final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setLogger(logger);
        serverConfigBuilder.setHubUrl(loginRestModel.getHubUrl());
        serverConfigBuilder.setPassword(loginRestModel.getHubPassword());
        serverConfigBuilder.setUsername(loginRestModel.getHubUsername());
        serverConfigBuilder.setTimeout(loginRestModel.getHubTimeout());
        serverConfigBuilder.setAlwaysTrustServerCertificate(Boolean.valueOf(loginRestModel.getHubAlwaysTrustCertificate()));
        serverConfigBuilder.setProxyHost(loginRestModel.getHubProxyHost());
        serverConfigBuilder.setProxyPort(loginRestModel.getHubProxyPort());
        serverConfigBuilder.setProxyUsername(loginRestModel.getHubProxyUsername());
        serverConfigBuilder.setProxyPassword(loginRestModel.getHubProxyPassword());

        validateHubConfiguration(serverConfigBuilder);
        final RestConnection restConnection = createRestConnection(serverConfigBuilder);
        restConnection.connect();
        logger.info("Connected");

        final boolean isValidLoginUser = isUserRoleValid(loginRestModel.getHubUsername(), restConnection);
        if (isValidLoginUser) {
            final Authentication authentication = new UsernamePasswordAuthenticationToken(loginRestModel.getHubUsername(), loginRestModel.getHubPassword(), Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication.isAuthenticated();
        }

        logger.info("User role not authenticated");
        return false;
    }

    public boolean isUserRoleValid(final String userName, final RestConnection restConnection) {
        final HubServicesFactory hubServicesFactory = new HubServicesFactory(restConnection);
        final UserDataService userDataService = hubServicesFactory.createUserDataService();

        try {
            final List<RoleView> userRoles = userDataService.getRolesForUser(userName);
            for (final RoleView roles : userRoles) {
                if ("System Administrator".equalsIgnoreCase(roles.name)) {
                    return true;
                }
            }
        } catch (final IntegrationException e) {
            return false;
        }

        return false;
    }

    public void validateHubConfiguration(final HubServerConfigBuilder hubServerConfigBuilder) throws AlertFieldException {
        final AbstractValidator validator = hubServerConfigBuilder.createValidator();
        final ValidationResults results = validator.assertValid();
        if (!results.getResultMap().isEmpty()) {
            final Map<String, String> fieldErrors = new HashMap<>();
            for (final Entry<FieldEnum, Set<ValidationResult>> result : results.getResultMap().entrySet()) {
                final Set<ValidationResult> validationResult = result.getValue();
                final List<String> errors = new ArrayList<>();
                for (final ValidationResult currentValidationResult : validationResult) {
                    errors.add(currentValidationResult.getMessage());
                }

                fieldErrors.put(result.getKey().getKey(), StringUtils.join(errors, " , "));
            }
            throw new AlertFieldException("There were issues with the configuration.", fieldErrors);
        }
    }

    public RestConnection createRestConnection(final HubServerConfigBuilder hubServerConfigBuilder) throws IntegrationException {
        final HubServerConfig hubServerConfig = hubServerConfigBuilder.build();
        return hubServerConfig.createCredentialsRestConnection(hubServerConfigBuilder.getLogger());
    }
}
