/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.ldap;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import com.synopsys.integration.alert.component.authentication.security.UserManagementAuthoritiesPopulator;

public class MappingLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {
    private final Logger logger = LoggerFactory.getLogger(MappingLdapAuthoritiesPopulator.class);
    private final UserManagementAuthoritiesPopulator authoritiesPopulator;

    public MappingLdapAuthoritiesPopulator(ContextSource contextSource, String groupSearchBase, UserManagementAuthoritiesPopulator authoritiesPopulator) {
        super(contextSource, groupSearchBase);
        this.authoritiesPopulator = authoritiesPopulator;
    }

    @Override
    public Set<GrantedAuthority> getGroupMembershipRoles(String userDn, String userName) {
        /*
            The parent class adds the additional roles to the set of roles returned by this method.
            It needs to return a mutable set.
         */
        Set<GrantedAuthority> grantedAuthorities = new LinkedHashSet<>();
        try {
            grantedAuthorities.addAll(super.getGroupMembershipRoles(userDn, userName));
        } catch (Exception ex) {
            logger.error("Error determining LDAP group membership.", ex);
        }
        return grantedAuthorities;
    }

    @Override
    protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {
        // load the roles from the database.  These roles will be added to the set of roles for the user.
        return authoritiesPopulator.addAdditionalRoles(username, Set.of());
    }
}
