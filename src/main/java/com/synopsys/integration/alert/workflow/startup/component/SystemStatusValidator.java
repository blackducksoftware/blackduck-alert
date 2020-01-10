/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.workflow.startup.component;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.SystemStatusUtility;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;

@Component
@Order(3)
public class SystemStatusValidator extends StartupComponent {
    private final SystemStatusUtility systemStatusUtility;
    private final EncryptionUtility encryptionUtility;
    private final UserAccessor userAccessor;

    @Autowired
    public SystemStatusValidator(SystemStatusUtility systemStatusUtility, EncryptionUtility encryptionUtility, UserAccessor userAccessor) {
        this.systemStatusUtility = systemStatusUtility;
        this.encryptionUtility = encryptionUtility;
        this.userAccessor = userAccessor;
    }

    @Override
    protected void initialize() {
        boolean systemInitializationStatus = isSystemInitialized();
        systemStatusUtility.setSystemInitialized(systemInitializationStatus);
    }

    private boolean isSystemInitialized() {
        return areEncryptionSettingsConfigured() && isAdminUserConfigured();
    }

    private boolean areEncryptionSettingsConfigured() {
        return encryptionUtility.isInitialized();

    }

    private boolean isAdminUserConfigured() {
        return userAccessor
                   .getUser(DefaultUserAccessor.DEFAULT_ADMIN_USER)
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
