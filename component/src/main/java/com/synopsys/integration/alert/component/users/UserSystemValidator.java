/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
