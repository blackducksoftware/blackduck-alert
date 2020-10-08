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
package com.synopsys.integration.alert.web.security.authentication.event;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.core.impl.NameIDImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml.SAMLAuthenticationToken;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

@Component
public class AuthenticationEventManager {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationEventManager.class);
    private EventManager eventManager;

    @Autowired
    public AuthenticationEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void sendAuthenticationEvent(Authentication authentication, AuthenticationType authenticationType) {
        String username;
        String emailAddress = null;
        try {
            if (authentication instanceof SAMLAuthenticationToken) {
                SAMLAuthenticationToken samlAuthenticationToken = (SAMLAuthenticationToken) authentication;
                SAMLMessageContext credentials = samlAuthenticationToken.getCredentials();
                NameIDImpl subjectNameIdentifier = (NameIDImpl) credentials.getSubjectNameIdentifier();
                username = subjectNameIdentifier.getValue();
                emailAddress = username;
            } else {
                username = authentication.getName();
            }
            sendAuthenticationEvent(username, emailAddress, authenticationType, authentication.getAuthorities());
        } catch (Exception e) {
            logger.warn("Unable to send authentication event");
            logger.debug("Authentication event failure", e);
        }
    }

    public Optional<String> getRoleFromAuthority(GrantedAuthority grantedAuthority) {
        String authority = grantedAuthority.getAuthority();
        if (authority.startsWith(UserModel.ROLE_PREFIX)) {
            String roleName = StringUtils.substringAfter(authority, UserModel.ROLE_PREFIX);
            return Optional.of(roleName);
        }
        return Optional.empty();
    }

    private void sendAuthenticationEvent(String username, String emailAddress, AuthenticationType authenticationType, Collection<? extends GrantedAuthority> authorities) throws AlertException {
        if (username == null) {
            throw new AlertException("Unable to send authentication event with null username");
        }
        Set<UserRoleModel> alertRoles = authorities
                                            .stream()
                                            .map(this::getRoleFromAuthority)
                                            .flatMap(Optional::stream)
                                            .map(UserRoleModel::of)
                                            .collect(Collectors.toSet());
        // The database users will not be enabled because they already exist in the database when this is called. So a new entry will not be added to the database.
        UserModel userModel = UserModel.newUser(username, null, emailAddress, authenticationType, alertRoles, true);
        AlertAuthenticationEvent authEvent = new AlertAuthenticationEvent(userModel);
        eventManager.sendEvent(authEvent);
    }

}
