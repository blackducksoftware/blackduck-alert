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

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.web.HubAuthenticationManager;
import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

@RestController
public class LoginController extends ConfigController<LoginRestModel> {

    private final HubAuthenticationManager authenticationManager;

    @Autowired
    public LoginController(final HubAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody(required = false) final LoginRestModel loginRestModel) {
        final Authentication preAuthentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Pre authenticated " + preAuthentication.isAuthenticated() + " : " + preAuthentication.getPrincipal() + " : " + preAuthentication.getCredentials());

        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
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
            final HubServerConfig configServer = serverConfigBuilder.build();
            final CredentialsRestConnection restConnection = configServer.createCredentialsRestConnection(logger);
            restConnection.connect();
            System.out.println("Connected");
            // TODO check User's role
            final Authentication authentication = new UsernamePasswordAuthenticationToken(loginRestModel.getHubUsername(), loginRestModel.getHubPassword(), Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("{\"message\":\"Success\"}", HttpStatus.ACCEPTED);
    }

    @Override
    public List<LoginRestModel> getConfig(final Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> postConfig(final LoginRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> putConfig(final LoginRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> validateConfig(final LoginRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> deleteConfig(final LoginRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> testConfig(final LoginRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

}
