/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.security.authentication;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class AlertAuthenticationProvider implements AuthenticationProvider {
    private static final Logger logger = LoggerFactory.getLogger(AlertAuthenticationProvider.class);
    private List<AuthenticationPerformer> authenticationPerformers;

    @Autowired
    public AlertAuthenticationProvider(List<AuthenticationPerformer> authenticationPerformers) {
        this.authenticationPerformers = authenticationPerformers;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            throw new IllegalArgumentException("Only UsernamePasswordAuthenticationToken is supported, " + authentication.getClass() + " was attempted");
        }

        for (AuthenticationPerformer authenticationPerformer : authenticationPerformers) {
            try {
                Optional<Authentication> completedAuthentication = authenticationPerformer.performAuthentication(authentication)
                                                                       .filter(Authentication::isAuthenticated);
                if (completedAuthentication.isPresent()) {
                    return completedAuthentication.get();
                }
            } catch (Exception ex) {
                String authTypeError = String.format("Error with with authentication type %s - cause: ", authenticationPerformer.getAuthenticationType());
                logger.error(authTypeError, ex);
            }
        }

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
