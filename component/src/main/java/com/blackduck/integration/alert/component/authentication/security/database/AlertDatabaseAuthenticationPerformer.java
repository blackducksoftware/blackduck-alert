/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.security.database;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.security.AuthenticationPerformer;
import com.blackduck.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.blackduck.integration.alert.common.enumeration.AuthenticationType;
import com.blackduck.integration.alert.common.persistence.accessor.UserAccessor;
import com.blackduck.integration.alert.common.persistence.model.UserModel;

@Component
public class AlertDatabaseAuthenticationPerformer extends AuthenticationPerformer {
    private final Logger logger = LoggerFactory.getLogger(AlertDatabaseAuthenticationPerformer.class);

    private final DaoAuthenticationProvider alertDatabaseAuthProvider;
    private final UserAccessor userAccessor;

    private final long maximumFailedAttempts;
    private final long lockoutDurationInSeconds;

    @Autowired
    public AlertDatabaseAuthenticationPerformer(
        AuthenticationEventManager authenticationEventManager,
        RoleAccessor roleAccessor,
        DaoAuthenticationProvider alertDatabaseAuthProvider,
        UserAccessor userAccessor,
        AlertProperties alertProperties
    ) {
        super(authenticationEventManager, roleAccessor);
        this.alertDatabaseAuthProvider = alertDatabaseAuthProvider;
        this.userAccessor = userAccessor;
        this.maximumFailedAttempts = alertProperties.getLoginLockoutThreshold();
        this.lockoutDurationInSeconds = TimeUnit.MINUTES.toSeconds(alertProperties.getLoginLockoutMinutes());
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.DATABASE;
    }

    @Override
    public Authentication authenticateWithProvider(Authentication pendingAuthentication) {
        logger.info("Attempting database authentication...");
        String userName = pendingAuthentication.getName();
        UserModel userModel = userAccessor.getUser(userName).orElseThrow(() -> new BadCredentialsException("Invalid user credentials."));
        userModel = checkAndUnlockAccount(userModel).orElseThrow(() -> new BadCredentialsException("Invalid user credentials."));
        Authentication userAuthentication;
        try {
            userAuthentication = alertDatabaseAuthProvider.authenticate(pendingAuthentication);
        } catch (BadCredentialsException ex) {
            logger.error(ex.getMessage(), ex);
            userAuthentication = pendingAuthentication;
            userAuthentication.setAuthenticated(false);
        }
        updateLoginStats(userModel, userAuthentication.isAuthenticated());

        return userAuthentication;
    }

    private Optional<UserModel> checkAndUnlockAccount(UserModel existingUser) {
        if (!existingUser.isLocked()) {
            return Optional.of(existingUser);
        }

        Duration durationFromLastFailedLogin = Duration.between(existingUser.getLastFailedLogin().orElse(OffsetDateTime.now()), OffsetDateTime.now());
        boolean remainLocked = Math.abs(durationFromLastFailedLogin.toSeconds()) < lockoutDurationInSeconds;
        long failedLoginAttempts = existingUser.getFailedLoginAttempts();
        // if account should be unlocked reset the failed login attempts to 0.
        if (!remainLocked) {
            failedLoginAttempts = 0;
        }
        UserModel updatedUser = UserModel.existingUser(
            existingUser.getId(),
            existingUser.getName(),
            existingUser.getPassword(),
            existingUser.getEmailAddress(),
            existingUser.getAuthenticationType(),
            existingUser.getRoles(),
            remainLocked,
            existingUser.isEnabled(),
            existingUser.getLastLogin().orElse(null),
            existingUser.getLastFailedLogin().orElse(null),
            failedLoginAttempts
        );
        return updateUserModel(updatedUser);
    }

    private void updateLoginStats(UserModel userModel, boolean authenticated) {
        long failedLoginAttempts = userModel.getFailedLoginAttempts();
        OffsetDateTime lastLogin = userModel.getLastLogin().orElse(null);
        OffsetDateTime lastFailedLogin = userModel.getLastFailedLogin().orElse(null);
        if (!authenticated) {
            failedLoginAttempts++;
            lastFailedLogin = OffsetDateTime.now();
        } else {
            failedLoginAttempts = 0;
            lastLogin = OffsetDateTime.now();
        }
        UserModel updatedUser = UserModel.existingUser(
            userModel.getId(),
            userModel.getName(),
            userModel.getPassword(),
            userModel.getEmailAddress(),
            userModel.getAuthenticationType(),
            userModel.getRoles(),
            failedLoginAttempts >= maximumFailedAttempts,
            userModel.isEnabled(),
            lastLogin,
            lastFailedLogin,
            failedLoginAttempts
        );
        updateUserModel(updatedUser);
    }

    private Optional<UserModel> updateUserModel(UserModel updatedUser) {
        try {
            return Optional.ofNullable(userAccessor.updateUser(updatedUser, true));
        } catch (AlertException ex) {
            logger.error("Error authenticating user", ex);
        }
        return Optional.empty();
    }
}
