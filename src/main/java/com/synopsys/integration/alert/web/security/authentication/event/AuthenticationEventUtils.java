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
package com.synopsys.integration.alert.web.security.authentication.event;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.UserRole;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

@Component
public class AuthenticationEventUtils {
    private EventManager eventManager;

    @Autowired
    public AuthenticationEventUtils(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void sendAuthenticationEvent(Authentication authentication) {
        UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) authentication;
        String username = userToken.getName();
        String emailAddress = null; // FIXME determine how to get an email address
        Set<UserRoleModel> alertRoles = authentication.getAuthorities()
                                            .stream()
                                            .map(this::getRoleFromAuthority)
                                            .flatMap(Optional::stream)
                                            .map(UserRole::name)
                                            .map(UserRoleModel::of)
                                            .collect(Collectors.toSet());

        UserModel userModel = UserModel.of(username, null, emailAddress, alertRoles);
        AlertAuthenticationEvent authEvent = new AlertAuthenticationEvent(userModel);
        eventManager.sendEvent(authEvent);
    }

    public Optional<UserRole> getRoleFromAuthority(GrantedAuthority grantedAuthority) {
        String authority = grantedAuthority.getAuthority();
        if (authority.startsWith(UserModel.ROLE_PREFIX)) {
            String alertRoleCandidate = StringUtils.substringAfter(authority, UserModel.ROLE_PREFIX);
            return UserRole.findUserRole(alertRoleCandidate);
        }
        return Optional.empty();
    }

}
