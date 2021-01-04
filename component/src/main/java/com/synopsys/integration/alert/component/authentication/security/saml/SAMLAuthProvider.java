/**
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
package com.synopsys.integration.alert.component.authentication.security.saml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLAuthenticationProvider;

import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.component.authentication.security.event.AuthenticationEventManager;

public class SAMLAuthProvider extends SAMLAuthenticationProvider {
    private final Logger logger = LoggerFactory.getLogger(SAMLAuthProvider.class);
    private AuthenticationEventManager authenticationEventManager;

    public SAMLAuthProvider(AuthenticationEventManager authenticationEventManager) {
        this.authenticationEventManager = authenticationEventManager;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication currentAuth = super.authenticate(authentication);
        logger.debug("User authenticated: {}", currentAuth.isAuthenticated());
        if (currentAuth.isAuthenticated()) {
            authenticationEventManager.sendAuthenticationEvent(currentAuth, AuthenticationType.SAML);
            SecurityContextHolder.getContext().setAuthentication(currentAuth);
        }
        return currentAuth;
    }

}
