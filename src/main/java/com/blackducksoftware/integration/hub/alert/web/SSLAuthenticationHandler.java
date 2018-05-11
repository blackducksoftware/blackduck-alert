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
package com.blackducksoftware.integration.hub.alert.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.blackducksoftware.integration.hub.alert.web.controller.BaseController;

@EnableWebSecurity
@Configuration
@ConditionalOnProperty(name = "blackduck.alert.ssl.enable", havingValue = "true", relaxedNames = false)
public class SSLAuthenticationHandler extends WebSecurityConfigurerAdapter {

    private final HttpSessionCsrfTokenRepository csrfTokenRepository;

    @Autowired
    public SSLAuthenticationHandler(final HttpSessionCsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final String[] allowedPaths = {
                "/",
                "/#",
                "/favicon.ico",
                "/fonts/**",
                "/js/bundle.js",
                "/js/bundle.js.map",
                "/css/style.css",
                "index.html",
                BaseController.BASE_PATH + "/login",
                BaseController.BASE_PATH + "/logout",
                BaseController.BASE_PATH + "/about" };

        final String[] csrfIgnoredPaths = {
                "/",
                "/#",
                "/favicon.ico",
                "/fonts/**",
                "/js/bundle.js",
                "/js/bundle.js.map",
                "/css/style.css",
                "index.html",
                BaseController.BASE_PATH + "/login",
                BaseController.BASE_PATH + "/verify",
                BaseController.BASE_PATH + "/about" };
        http.requiresChannel().anyRequest().requiresSecure()
                .and().csrf().csrfTokenRepository(csrfTokenRepository).ignoringAntMatchers(csrfIgnoredPaths)
                .and().authorizeRequests().antMatchers(allowedPaths).permitAll()
                .and().authorizeRequests().anyRequest().hasRole("ADMIN")
                .and().logout().logoutSuccessUrl("/");
    }
}
