/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

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
import com.synopsys.integration.alert.common.security.UserPrincipal;
import com.synopsys.integration.alert.component.authentication.security.UserManagementAuthoritiesPopulator;

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
        Set<String> existingRoles = Set.of();
        if (alertRoles != null) {
            existingRoles = Arrays.stream(alertRoles).collect(Collectors.toSet());
        }
        Set<String> roleNames = authoritiesPopulator.addAdditionalRoleNames(userName, existingRoles, false);
        Set<UserRoleModel> roles = roleNames.stream()
                                       .map(UserRoleModel::of)
                                       .collect(Collectors.toSet());

        UserModel userModel = UserModel.newUser(userName, "", emailAddress, AuthenticationType.SAML, roles, true);
        return new UserPrincipal(userModel);
    }

}
