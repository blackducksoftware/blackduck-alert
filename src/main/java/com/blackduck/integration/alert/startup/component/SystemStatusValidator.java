/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.startup.component;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.persistence.accessor.SystemStatusAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.UserAccessor;
import com.blackduck.integration.alert.common.persistence.model.UserModel;
import com.blackduck.integration.alert.common.security.EncryptionUtility;

@Component
@Order(20)
public class SystemStatusValidator extends StartupComponent {
    private final SystemStatusAccessor systemStatusAccessor;
    private final EncryptionUtility encryptionUtility;
    private final UserAccessor userAccessor;

    @Autowired
    public SystemStatusValidator(SystemStatusAccessor systemStatusAccessor, EncryptionUtility encryptionUtility, UserAccessor userAccessor) {
        this.systemStatusAccessor = systemStatusAccessor;
        this.encryptionUtility = encryptionUtility;
        this.userAccessor = userAccessor;
    }

    @Override
    protected void initialize() {
        boolean systemInitializationStatus = isSystemInitialized();
        systemStatusAccessor.setSystemInitialized(systemInitializationStatus);
    }

    private boolean isSystemInitialized() {
        return areEncryptionSettingsConfigured() && isAdminUserConfigured();
    }

    private boolean areEncryptionSettingsConfigured() {
        return encryptionUtility.isInitialized();

    }

    private boolean isAdminUserConfigured() {
        return userAccessor
                   .getUser(UserAccessor.DEFAULT_ADMIN_USER_ID)
                   .filter(this::isUserEmailSet)
                   .filter(this::isUserPasswordSet)
                   .isPresent();
    }

    private boolean isUserEmailSet(UserModel user) {
        return StringUtils.isNotBlank(user.getEmailAddress());
    }

    private boolean isUserPasswordSet(UserModel user) {
        return StringUtils.isNotBlank(user.getPassword());
    }

}
