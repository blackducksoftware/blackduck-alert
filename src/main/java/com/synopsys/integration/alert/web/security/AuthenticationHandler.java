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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
@Profile("!ssl")
public class AuthenticationHandler extends WebSecurityConfigurerAdapter {
    public static final String RESET_PASSWORD_PATH = "/resetPassword/**";
    public static final String RESET_PASSWORD_WITH_USERNAME_PATH = "/resetPassword/**";
    public static final String H2_CONSOLE_PATH = "/h2/**";

    private final HttpPathManager httpPathManager;

    @Autowired
    AuthenticationHandler(final HttpPathManager httpPathManager) {
        this.httpPathManager = httpPathManager;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        ignorePaths(H2_CONSOLE_PATH, RESET_PASSWORD_PATH, RESET_PASSWORD_WITH_USERNAME_PATH);
        httpPathManager.completeHttpSecurity(http);
        // The profile above ensures that this will not be used if SSL is enabled.
        http.headers().frameOptions().disable();
    }

    private void ignorePaths(final String... paths) {
        for (final String path : paths) {
            httpPathManager.addAllowedPath(path);
            httpPathManager.addCsrfIgnoredPath(path);
        }
    }

}
