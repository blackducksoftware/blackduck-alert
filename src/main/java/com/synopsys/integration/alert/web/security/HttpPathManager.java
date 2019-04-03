/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.security;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.user.UserRole;
import com.synopsys.integration.alert.web.controller.BaseController;

@Component
public class HttpPathManager {
    private static final String[] DEFAULT_PATHS = {
        "/",
        "/#",
        "/favicon.ico",
        "/fonts/**",
        "/js/bundle.js",
        "/js/bundle.js.map",
        "/css/style.css",
        "index.html",
        "/saml/**",
        BaseController.BASE_PATH + "/about",
        BaseController.BASE_PATH + "/login",
        BaseController.BASE_PATH + "/logout",
        BaseController.BASE_PATH + "/resetPassword",
        BaseController.BASE_PATH + "/resetPassword/**",
        BaseController.BASE_PATH + "/system/messages/latest",
        BaseController.BASE_PATH + "/system/setup/initial"
    };
    private final List<String> allowedPaths;
    private final List<String> csrfIgnoredPaths;
    private final HttpSessionCsrfTokenRepository csrfTokenRepository;
    private final MetadataGeneratorFilter metadataGeneratorFilter;
    private final FilterChainProxy samlFilter;
    private final SAMLEntryPoint samlEntryPoint;

    @Autowired
    public HttpPathManager(final HttpSessionCsrfTokenRepository csrfTokenRepository, final MetadataGeneratorFilter metadataGeneratorFilter, final FilterChainProxy samlFilter, final SAMLEntryPoint samlEntryPoint) {
        this.csrfTokenRepository = csrfTokenRepository;
        this.metadataGeneratorFilter = metadataGeneratorFilter;
        this.samlFilter = samlFilter;
        this.samlEntryPoint = samlEntryPoint;
        allowedPaths = createDefaultAllowedPaths();
        csrfIgnoredPaths = createDefaultCsrfIgnoredPaths();
    }

    private List<String> createDefaultPaths() {
        final List<String> list = new LinkedList<>();
        for (final String path : DEFAULT_PATHS) {
            list.add(path);
        }
        return list;
    }

    private List<String> createDefaultAllowedPaths() {
        return createDefaultPaths();
    }

    private List<String> createDefaultCsrfIgnoredPaths() {
        return createDefaultPaths();
    }

    public void addAllowedPath(final String path) {
        allowedPaths.add(path);
    }

    public void addCsrfIgnoredPath(final String path) {
        csrfIgnoredPaths.add(path);
    }

    public String[] getAllowedPaths() {
        final String[] allowedPathArray = new String[allowedPaths.size()];
        return allowedPaths.toArray(allowedPathArray);
    }

    public String[] getCsrfIgnoredPaths() {
        final String[] csrfIgnoredPathArray = new String[csrfIgnoredPaths.size()];
        return csrfIgnoredPaths.toArray(csrfIgnoredPathArray);
    }

    public void completeHttpSecurity(final HttpSecurity http) throws Exception {
        http.exceptionHandling().authenticationEntryPoint(samlEntryPoint)
            .and().csrf().csrfTokenRepository(csrfTokenRepository).ignoringAntMatchers(getCsrfIgnoredPaths())
            .and().addFilterBefore(metadataGeneratorFilter, ChannelProcessingFilter.class)
            .addFilterAfter(samlFilter, BasicAuthenticationFilter.class)
            .authorizeRequests().antMatchers(getAllowedPaths()).permitAll()
            .and().authorizeRequests().anyRequest().hasRole(UserRole.ALERT_ADMIN.name())
            .and().logout().logoutSuccessUrl("/");
    }
}
