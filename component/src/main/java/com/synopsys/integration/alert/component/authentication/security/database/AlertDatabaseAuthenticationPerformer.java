/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.component.authentication.security.AuthenticationPerformer;
import com.synopsys.integration.alert.component.authentication.security.event.AuthenticationEventManager;

@Component
public class AlertDatabaseAuthenticationPerformer extends AuthenticationPerformer {
    private final Logger logger = LoggerFactory.getLogger(AlertDatabaseAuthenticationPerformer.class);

    private DaoAuthenticationProvider alertDatabaseAuthProvider;

    @Autowired
    public AlertDatabaseAuthenticationPerformer(AuthenticationEventManager authenticationEventManager, RoleAccessor roleAccessor, DaoAuthenticationProvider alertDatabaseAuthProvider) {
        super(authenticationEventManager, roleAccessor);
        this.alertDatabaseAuthProvider = alertDatabaseAuthProvider;
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.DATABASE;
    }

    @Override
    public Authentication authenticateWithProvider(Authentication pendingAuthentication) {
        logger.info("Attempting database authentication...");
        return alertDatabaseAuthProvider.authenticate(pendingAuthentication);
    }

}
