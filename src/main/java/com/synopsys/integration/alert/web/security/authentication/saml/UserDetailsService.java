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
package com.synopsys.integration.alert.web.security.authentication.saml;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.web.security.authentication.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.web.security.authentication.database.UserPrincipal;

public class UserDetailsService implements SAMLUserDetailsService {
    private final UserManagementAuthoritiesPopulator authoritiesPopulator;

    public UserDetailsService(UserManagementAuthoritiesPopulator authoritiesPopulator) {
        this.authoritiesPopulator = authoritiesPopulator;
    }

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        String userName = credential.getNameID().getValue();
        String emailAddress = StringUtils.contains(userName, "@") ? userName : null;
        String[] alertRoles = credential.getAttributeAsStringArray(authoritiesPopulator.getSAMLRoleAttributeName("AlertRoles"));
        Set<UserRoleModel> roles = Set.of();

        if (alertRoles != null) {
            Set<String> roleNames = authoritiesPopulator.addAdditionalRoleNames(userName, Arrays.stream(alertRoles).collect(Collectors.toSet()), false);
            roles = roleNames.stream()
                        .map(UserRoleModel::of)
                        .collect(Collectors.toSet());
        }

        UserModel userModel = UserModel.newUser(userName, "", emailAddress, AuthenticationType.SAML, roles);
        return new UserPrincipal(userModel);
    }
}
