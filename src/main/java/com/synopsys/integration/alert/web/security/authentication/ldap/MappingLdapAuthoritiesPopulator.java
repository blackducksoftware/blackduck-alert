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
package com.synopsys.integration.alert.web.security.authentication.ldap;

import java.util.Set;

import org.springframework.ldap.core.ContextSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import com.synopsys.integration.alert.web.security.authentication.UserManagementAuthoritiesPopulator;

public class MappingLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {
    private final UserManagementAuthoritiesPopulator authoritiesPopulator;

    public MappingLdapAuthoritiesPopulator(ContextSource contextSource, String groupSearchBase, UserManagementAuthoritiesPopulator authoritiesPopulator) {
        super(contextSource, groupSearchBase);
        this.authoritiesPopulator = authoritiesPopulator;
    }

    @Override
    public Set<GrantedAuthority> getGroupMembershipRoles(String userDn, String userName) {
        return authoritiesPopulator.addAdditionalRoles(userName, super.getGroupMembershipRoles(userDn, userName));
    }
}
