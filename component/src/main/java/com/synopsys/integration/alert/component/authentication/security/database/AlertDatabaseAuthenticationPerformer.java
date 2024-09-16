/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.database;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.authentication.security.AuthenticationPerformer;
import com.synopsys.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;

@Component
public class AlertDatabaseAuthenticationPerformer extends AuthenticationPerformer {
    private final Logger logger = LoggerFactory.getLogger(AlertDatabaseAuthenticationPerformer.class);

    private final DaoAuthenticationProvider alertDatabaseAuthProvider;
    private final UserAccessor userAccessor;

    @Autowired
    public AlertDatabaseAuthenticationPerformer(
        AuthenticationEventManager authenticationEventManager,
        RoleAccessor roleAccessor,
        DaoAuthenticationProvider alertDatabaseAuthProvider,
        UserAccessor userAccessor
    ) {
        super(authenticationEventManager, roleAccessor);
        this.alertDatabaseAuthProvider = alertDatabaseAuthProvider;
        this.userAccessor = userAccessor;
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.DATABASE;
    }

    @Override
    public Authentication authenticateWithProvider(Authentication pendingAuthentication) {
        logger.info("Attempting database authentication...");
        Authentication userAuthentication = alertDatabaseAuthProvider.authenticate(pendingAuthentication);

        if (!userAuthentication.isAuthenticated()) {
            String userName = userAuthentication.getName();
            lockUserAccount(userName);
        }

        return userAuthentication;
    }

    private void lockUserAccount(String userName) {
        try {
            Optional<UserModel> userModel = userAccessor.getUser(userName);
            if (userModel.isPresent()) {
                UserModel existingUser = userModel.get();
                long failedLoginAttempts = existingUser.getFailedLoginCount() + 1;
                UserModel updatedUser = UserModel.existingUser(
                    existingUser.getId(),
                    existingUser.getName(),
                    existingUser.getPassword(),
                    existingUser.getEmailAddress(),
                    existingUser.getAuthenticationType(),
                    existingUser.getRoles(),
                    failedLoginAttempts > 10,
                    existingUser.isEnabled(),
                    existingUser.getLastLogin(),
                    OffsetDateTime.now(),
                    failedLoginAttempts
                );

                userAccessor.updateUser(updatedUser, true);
            }
        } catch (AlertException ex) {
            logger.error("Error authenticating user", ex);
        }
    }

}
