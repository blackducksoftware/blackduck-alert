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
package com.synopsys.integration.alert.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@EnableWebSecurity
@Configuration
@Profile("!ssl")
public class AuthenticationHandler extends WebSecurityConfigurerAdapter {
    public static final String H2_CONSOLE_PATH = "/h2/**";
    private final HttpSessionCsrfTokenRepository csrfTokenRepository;
    private final HttpPathManager httpPathManager;

    @Autowired
    AuthenticationHandler(final HttpSessionCsrfTokenRepository csrfTokenRepository, final HttpPathManager httpPathManager) {
        this.csrfTokenRepository = csrfTokenRepository;
        this.httpPathManager = httpPathManager;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        httpPathManager.addAllowedPath(H2_CONSOLE_PATH);
        httpPathManager.addCsrfIgnoredPath(H2_CONSOLE_PATH);
        final String[] allowedPaths = httpPathManager.getAllowedPaths();
        final String[] csrfIgnoredPaths = httpPathManager.getCsrfIgnoredPaths();

        http.csrf().csrfTokenRepository(csrfTokenRepository).ignoringAntMatchers(csrfIgnoredPaths)
            .and().authorizeRequests().antMatchers(allowedPaths).permitAll()
            .and().authorizeRequests().anyRequest().hasRole("ADMIN")
            .and().logout().logoutSuccessUrl("/");
        // The profile above ensures that this will not be used if SSL is enabled.
        http.headers().frameOptions().disable();
    }

}
