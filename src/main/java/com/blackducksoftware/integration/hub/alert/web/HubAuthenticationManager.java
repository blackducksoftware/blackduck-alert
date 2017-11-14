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
package com.blackducksoftware.integration.hub.alert.web;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;

@Component
public class HubAuthenticationManager implements AuthenticationManager {

    public GlobalConfigRestModel global;

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();
        System.out.println("CREDENTIALS " + username + ":" + password);
        //
        // final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        //
        // try {
        // final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        // serverConfigBuilder.setLogger(logger);
        // serverConfigBuilder.setHubUrl(global.getHubUrl());
        // serverConfigBuilder.setPassword(global.getHubPassword());
        // serverConfigBuilder.setUsername(global.getHubUsername());
        // serverConfigBuilder.setTimeout(global.getHubTimeout());
        // serverConfigBuilder.setAlwaysTrustServerCertificate(Boolean.valueOf(global.getHubAlwaysTrustCertificate()));
        // serverConfigBuilder.setProxyHost(global.getHubProxyHost());
        // serverConfigBuilder.setProxyPort(global.getHubProxyPort());
        // serverConfigBuilder.setProxyUsername(global.getHubProxyUsername());
        // serverConfigBuilder.setProxyPassword(global.getHubProxyPassword());
        // final HubServerConfig configServer = serverConfigBuilder.build();
        // final CredentialsRestConnection restConnection = configServer.createCredentialsRestConnection(logger);
        // restConnection.connect();
        // System.out.println("Connected");
        // authentication.setAuthenticated(true);
        // } catch (final Exception e) {
        // e.printStackTrace();
        // }

        return authentication;
    }

}
