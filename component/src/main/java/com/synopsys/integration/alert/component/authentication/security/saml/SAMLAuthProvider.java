/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLAuthenticationProvider;

import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.component.authentication.security.event.AuthenticationEventManager;

public class SAMLAuthProvider extends SAMLAuthenticationProvider {
    private final Logger logger = LoggerFactory.getLogger(SAMLAuthProvider.class);
    private AuthenticationEventManager authenticationEventManager;

    public SAMLAuthProvider(AuthenticationEventManager authenticationEventManager) {
        this.authenticationEventManager = authenticationEventManager;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication currentAuth = super.authenticate(authentication);
        logger.debug("User authenticated: {}", currentAuth.isAuthenticated());
        if (currentAuth.isAuthenticated()) {
            authenticationEventManager.sendAuthenticationEvent(currentAuth, AuthenticationType.SAML);
            SecurityContextHolder.getContext().setAuthentication(currentAuth);
        }
        return currentAuth;
    }

}
