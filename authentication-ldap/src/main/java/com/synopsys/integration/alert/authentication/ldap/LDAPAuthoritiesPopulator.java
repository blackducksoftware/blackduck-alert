/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.authentication.ldap;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;

@Component
public class LDAPAuthoritiesPopulator {
    private final UserAccessor userAccessor;

    @Autowired
    public LDAPAuthoritiesPopulator(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    public Set<GrantedAuthority> addAdditionalRoles(String userName, Set<GrantedAuthority> existingRoles) {
        Set<String> existingRoleNames = existingRoles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        Set<String> alertRoles = addAdditionalRoleNames(userName, existingRoleNames, true);
        return alertRoles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());

    }

    public Set<String> addAdditionalRoleNames(String userName, Set<String> existingRoles, boolean appendRolePrefix) {
        Set<String> rolesFromDB = getRolesFromDatabase(userName, appendRolePrefix);
        Set<String> roles = new LinkedHashSet<>(rolesFromDB);
        existingRoles
            .stream()
            .filter(StringUtils::isNotBlank)
            .forEach(roles::add);
        return roles;
    }

    private Set<String> getRolesFromDatabase(String userName, boolean appendRolePrefix) {
        Function<String, String> function = appendRolePrefix ? roleName -> UserModel.ROLE_PREFIX + roleName : Function.identity();
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        Set<String> roleNames = userModel.map(UserModel::getRoleNames).orElse(Set.of(DefaultUserRole.ALERT_USER.name()));
        Set<String> newRoleNames = new LinkedHashSet<>(roleNames.size());
        for (String roleName : roleNames) {
            newRoleNames.add(function.apply(roleName));
        }
        return newRoleNames;
    }

}
