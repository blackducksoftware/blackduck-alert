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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.web.HubAuthenticationManager;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.google.gson.Gson;

@RestController
public class LoginController {

    private final HubAuthenticationManager authenticationManager;

    @Autowired
    public LoginController(final HubAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(final HttpServletRequest request) {

        final Authentication preAuthentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Pre authenticated " + preAuthentication.isAuthenticated() + " : " + preAuthentication.getPrincipal() + " : " + preAuthentication.getCredentials());

        String body = null;
        final StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            final InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while (bufferedReader.ready()) {
                    stringBuilder.append(bufferedReader.readLine());
                }
            } else {
                stringBuilder.append("");
            }
        } catch (final IOException e) {
            // TODO handle exception
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
        body = stringBuilder.toString();

        final Gson gson = new Gson();
        final GlobalConfigRestModel global = gson.fromJson(body, GlobalConfigRestModel.class);
        // authenticationManager.global = global;
        //
        // try {
        // request.login(global.getHubUsername(), global.getHubPassword());
        // } catch (final ServletException e) {
        // e.printStackTrace();
        // }
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        try {
            final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
            serverConfigBuilder.setLogger(logger);
            serverConfigBuilder.setHubUrl(global.getHubUrl());
            serverConfigBuilder.setPassword(global.getHubPassword());
            serverConfigBuilder.setUsername(global.getHubUsername());
            serverConfigBuilder.setTimeout(global.getHubTimeout());
            serverConfigBuilder.setAlwaysTrustServerCertificate(Boolean.valueOf(global.getHubAlwaysTrustCertificate()));
            serverConfigBuilder.setProxyHost(global.getHubProxyHost());
            serverConfigBuilder.setProxyPort(global.getHubProxyPort());
            serverConfigBuilder.setProxyUsername(global.getHubProxyUsername());
            serverConfigBuilder.setProxyPassword(global.getHubProxyPassword());
            final HubServerConfig configServer = serverConfigBuilder.build();
            final CredentialsRestConnection restConnection = configServer.createCredentialsRestConnection(logger);
            restConnection.connect();
            System.out.println("Connected");
            // TODO check User's role
            final Authentication authentication = new UsernamePasswordAuthenticationToken(global.getHubUsername(), global.getHubPassword(), Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("{\"message\":\"Success\"}", HttpStatus.ACCEPTED);

        // final String referrer = request.getHeader("Referer");
        // if (referrer != null) {
        // request.getSession().setAttribute("url_prior_login", referrer);
        // }
        // return "{\"message\":\"Success\"}";
    }

}
