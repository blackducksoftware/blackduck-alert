/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.component.authentication.security.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.component.authentication.security.AuthenticationPerformer;
import com.synopsys.integration.alert.component.authentication.security.event.AuthenticationEventManager;

@Component
public class AlertDatabaseAuthenticationPerformer extends AuthenticationPerformer {
    private final Logger logger = LoggerFactory.getLogger(AlertDatabaseAuthenticationPerformer.class);

    private DaoAuthenticationProvider alertDatabaseAuthProvider;

    @Autowired
    public AlertDatabaseAuthenticationPerformer(AuthenticationEventManager authenticationEventManager, RoleAccessor roleAccessor, DaoAuthenticationProvider alertDatabaseAuthProvider) {
        super(authenticationEventManager, roleAccessor);
        this.alertDatabaseAuthProvider = alertDatabaseAuthProvider;
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.DATABASE;
    }

    @Override
    public Authentication authenticateWithProvider(Authentication pendingAuthentication) {
        logger.info("Attempting database authentication...");
        return alertDatabaseAuthProvider.authenticate(pendingAuthentication);
    }

}
