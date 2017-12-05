/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.FieldEnum;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResults;

@RestController
public class LoginController {
    @PostMapping(value = "/logout")
    public ResponseEntity<String> logout(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return new ResponseEntity<>("{\"message\":\"Success\"}", HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(final HttpServletRequest request, @RequestBody(required = false) final LoginRestModel loginRestModel) {
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);

        final HttpSession session = request.getSession(false);
        if (session != null) {
            // TODO figure out timeout
            session.setMaxInactiveInterval(60 * 10);
        }
        try {
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
            System.out.println("Connected");
            // TODO check User's role
            final Authentication authentication = new UsernamePasswordAuthenticationToken(loginRestModel.getHubUsername(), loginRestModel.getHubPassword(), Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return new ResponseEntity<>("{\"message\":\"Success\"}", HttpStatus.ACCEPTED);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(0L, e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
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

    protected ResponseEntity<String> createResponse(final HttpStatus status, final String message) {
        final String responseBody = new ResponseBodyBuilder(0L, message).build();
        return new ResponseEntity<>(responseBody, status);
    }

}
