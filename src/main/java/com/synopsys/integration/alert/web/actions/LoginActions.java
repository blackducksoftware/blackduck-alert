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
package com.synopsys.integration.alert.web.actions;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.web.model.LoginConfig;
import com.synopsys.integration.log.IntLogger;

@Component
public class LoginActions {
    private static final Logger logger = LoggerFactory.getLogger(LoginActions.class);
    private final DaoAuthenticationProvider alertDatabaseAuthProvider;

    @Autowired
    public LoginActions(final DaoAuthenticationProvider alertDatabaseAuthProvider) {
        this.alertDatabaseAuthProvider = alertDatabaseAuthProvider;
    }

    public boolean authenticateUser(final LoginConfig loginConfig, final IntLogger logger) {
        final Authentication pendingAuthentication = new UsernamePasswordAuthenticationToken(loginConfig.getBlackDuckUsername(), loginConfig.getBlackDuckPassword(), Collections.emptyList());
        final Authentication authentication = alertDatabaseAuthProvider.authenticate(pendingAuthentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication.isAuthenticated();
    }
}
