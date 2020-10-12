/**
 * component
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
package com.synopsys.integration.alert.component.users;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.system.BaseSystemValidator;

@Component
public class UserSystemValidator extends BaseSystemValidator {
    public static final String FIELD_ERROR_DEFAULT_USER_PWD = "Default admin user password missing";
    public static final String FIELD_ERROR_DEFAULT_USER_EMAIL = "Default admin user email missing";

    private final Logger logger = LoggerFactory.getLogger(UserSystemValidator.class);
    private UserAccessor userAccessor;

    @Autowired
    public UserSystemValidator(UserAccessor userAccessor, SystemMessageAccessor systemMessageAccessor) {
        super(systemMessageAccessor);
        this.userAccessor = userAccessor;
    }

    public void validateDefaultAdminUser(Long userId) {
        if (!userId.equals(UserAccessor.DEFAULT_ADMIN_USER_ID)) {
            return;
        }
        validateDefaultAdminUser();
    }

    public boolean validateDefaultAdminUser() {
        boolean valid = true;
        try {
            getSystemMessageAccessor().removeSystemMessagesByType(SystemMessageType.DEFAULT_ADMIN_USER_ERROR);
            Optional<UserModel> userModel = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
            boolean missingEmailAddress = userModel.map(UserModel::getEmailAddress).filter(StringUtils::isNotBlank).isEmpty();
            boolean missingEmailError = addSystemMessageForError(FIELD_ERROR_DEFAULT_USER_EMAIL, SystemMessageSeverity.ERROR, SystemMessageType.DEFAULT_ADMIN_USER_ERROR, missingEmailAddress);
            if (missingEmailError) {
                valid = false;
                logger.error(FIELD_ERROR_DEFAULT_USER_EMAIL);
            }

            boolean missingPassword = userModel.map(UserModel::getPassword).filter(StringUtils::isNotBlank).isEmpty();
            boolean missingPasswordError = addSystemMessageForError(FIELD_ERROR_DEFAULT_USER_PWD, SystemMessageSeverity.ERROR, SystemMessageType.DEFAULT_ADMIN_USER_ERROR, missingPassword);
            if (missingPasswordError) {
                valid = false;
                logger.error(FIELD_ERROR_DEFAULT_USER_PWD);
            }
        } catch (Exception e) {
            valid = false;
            logger.error("There was an unexpected error when attempting to validate the default admin user.", e);
        }
        return valid;
    }

}
