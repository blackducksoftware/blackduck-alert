/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.database;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
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
    public static final long DEFAULT_FAILED_ATTEMPTS = 10;
    // 15 minute lockout duration 15 * 60 = 900
    public static final long DEFAULT_LOCKOUT_DURATION_IN_SECONDS = 900;
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
        UserAccessor userAccessor
    ) {
        this(authenticationEventManager, roleAccessor, alertDatabaseAuthProvider, userAccessor, DEFAULT_FAILED_ATTEMPTS,
            DEFAULT_LOCKOUT_DURATION_IN_SECONDS
        );
    }

    protected AlertDatabaseAuthenticationPerformer(
        AuthenticationEventManager authenticationEventManager,
        RoleAccessor roleAccessor,
        DaoAuthenticationProvider alertDatabaseAuthProvider,
        UserAccessor userAccessor,
        long maximumFailedAttempts,
        long lockoutDurationInSeconds
    ) {
        super(authenticationEventManager, roleAccessor);
        this.alertDatabaseAuthProvider = alertDatabaseAuthProvider;
        this.userAccessor = userAccessor;
        this.maximumFailedAttempts = maximumFailedAttempts;
        this.lockoutDurationInSeconds = lockoutDurationInSeconds;
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
        if (userModel.isLocked()) {
            pendingAuthentication.setAuthenticated(false);
            return pendingAuthentication;
        }
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

        Duration durationFromLastFailedLogin = Duration.between(existingUser.getLastFailedLogin(), OffsetDateTime.now());
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
            existingUser.getLastLogin(),
            existingUser.getLastFailedLogin(),
            failedLoginAttempts
        );
        return updateUserModel(updatedUser);
    }

    private void updateLoginStats(UserModel userModel, boolean authenticated) {
        long failedLoginAttempts = userModel.getFailedLoginAttempts();
        OffsetDateTime lastLogin = userModel.getLastLogin();
        OffsetDateTime lastFailedLogin = userModel.getLastFailedLogin();
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
