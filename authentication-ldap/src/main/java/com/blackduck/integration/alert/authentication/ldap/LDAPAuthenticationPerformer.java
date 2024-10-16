/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.security.AuthenticationPerformer;
import com.blackduck.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.authentication.ldap.action.LDAPManager;
import com.blackduck.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.blackduck.integration.alert.common.enumeration.AuthenticationType;

@Component
public class LDAPAuthenticationPerformer extends AuthenticationPerformer {
    private final Logger logger = LoggerFactory.getLogger(LDAPAuthenticationPerformer.class);

    private final LDAPManager ldapManager;

    @Autowired
    public LDAPAuthenticationPerformer(AuthenticationEventManager authenticationEventManager, RoleAccessor roleAccessor, LDAPManager ldapManager) {
        super(authenticationEventManager, roleAccessor);
        this.ldapManager = ldapManager;
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.LDAP;
    }

    @Override
    public Authentication authenticateWithProvider(Authentication pendingAuthentication) {
        logger.info("Checking ldap based authentication...");
        Authentication result = pendingAuthentication;
        if (Boolean.TRUE.equals(ldapManager.isLDAPEnabled())) {
            logger.info("LDAP authentication enabled");
            try {
                Optional<LdapAuthenticationProvider> authenticationProvider = ldapManager.getAuthenticationProvider();
                if (authenticationProvider.isPresent()) {
                    result = authenticationProvider.get().authenticate(pendingAuthentication);
                }
            } catch (AlertConfigurationException ex) {
                logger.error("LDAP Configuration error", ex);
            } catch (LockedException ex) {
                throw ex;
            } catch (Exception ex) {
                logger.error("LDAP Authentication error", ex);
            }
        } else {
            logger.info("LDAP authentication disabled");
        }
        return result;
    }

}
