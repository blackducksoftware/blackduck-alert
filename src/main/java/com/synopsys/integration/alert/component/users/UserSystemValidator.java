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
package com.synopsys.integration.alert.component.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.BaseSystemValidator;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

@Component
public class UserSystemValidator extends BaseSystemValidator {
    private static final Logger logger = LoggerFactory.getLogger(UserSystemValidator.class);
    private UserAccessor userAccessor;

    @Autowired
    public UserSystemValidator(UserAccessor userAccessor, SystemMessageUtility systemMessageUtility) {
        super(systemMessageUtility);
        this.userAccessor = userAccessor;
    }

    public boolean validateSysadminUser() {
        getSystemMessageUtility().removeSystemMessagesByType(SystemMessageType.DEFAULT_ADMIN_USER_ERROR);

        Optional<UserModel> userModel = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        List<String> errors = new ArrayList();

        boolean hasEmailAddress = userModel.map(UserModel::getEmailAddress).filter(StringUtils::isNotBlank).isEmpty();
        validationCheck(SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_EMAIL, SystemMessageSeverity.ERROR, SystemMessageType.DEFAULT_ADMIN_USER_ERROR, hasEmailAddress)
            .ifPresent(error -> {
                logger.error(error);
                errors.add(error);
            });

        boolean isPasswordSet = userModel.map(UserModel::getPassword).filter(StringUtils::isNotBlank).isEmpty();
        validationCheck(SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_PWD, SystemMessageSeverity.ERROR, SystemMessageType.DEFAULT_ADMIN_USER_ERROR, isPasswordSet)
            .ifPresent(error -> {
                logger.error(error);
                errors.add(error);
            });

        return errors.size() == 0;
    }

}
