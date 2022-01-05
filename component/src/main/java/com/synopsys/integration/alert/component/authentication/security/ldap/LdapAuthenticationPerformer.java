/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.ldap;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.component.authentication.security.AuthenticationPerformer;
import com.synopsys.integration.alert.component.authentication.security.event.AuthenticationEventManager;

@Component
public class LdapAuthenticationPerformer extends AuthenticationPerformer {
    private final Logger logger = LoggerFactory.getLogger(LdapAuthenticationPerformer.class);

    private final LdapManager ldapManager;

    @Autowired
    public LdapAuthenticationPerformer(AuthenticationEventManager authenticationEventManager, RoleAccessor roleAccessor, LdapManager ldapManager) {
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
        if (ldapManager.isLdapEnabled()) {
            logger.info("LDAP authentication enabled");
            try {
                Optional<LdapAuthenticationProvider> authenticationProvider = ldapManager.getAuthenticationProvider();
                if (authenticationProvider.isPresent()) {
                    result = authenticationProvider.get().authenticate(pendingAuthentication);
                }
            } catch (AlertConfigurationException ex) {
                logger.error("LDAP Configuration error", ex);
            } catch (Exception ex) {
                logger.error("LDAP Authentication error", ex);
            }
        } else {
            logger.info("LDAP authentication disabled");
        }
        return result;
    }

}
