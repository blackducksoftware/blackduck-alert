/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.encryption.validator;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.system.BaseSystemValidator;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;

@Component
public class SettingsEncryptionValidator extends BaseSystemValidator {
    private static final String ENCRYPTION_PASSWORD_FIELD_NAME = "encryptionPassword";
    private static final String ENCRYPTION_GLOBAL_SALT_FIELD_NAME = "encryptionGlobalSalt";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public SettingsEncryptionValidator(EncryptionUtility encryptionUtility, SystemMessageAccessor systemMessageAccessor) {
        super(systemMessageAccessor);
        this.encryptionUtility = encryptionUtility;
    }

    public ValidationResponseModel validate(SettingsEncryptionModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        getSystemMessageAccessor().removeSystemMessagesByType(SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);

        Optional<String> encryptionPassword = model.getEncryptionPassword();
        Optional<String> encryptionGlobalSalt = model.getEncryptionGlobalSalt();

        boolean passwordExists = encryptionPassword.filter(StringUtils::isNotBlank).isPresent();
        boolean globalSaltExists = encryptionGlobalSalt.filter(StringUtils::isNotBlank).isPresent();

        // Verify the length of the model if the user passed in a value.
        minimumEncryptionFieldLength(statuses, ENCRYPTION_PASSWORD_FIELD_NAME, passwordExists, encryptionPassword::get);
        minimumEncryptionFieldLength(statuses, ENCRYPTION_GLOBAL_SALT_FIELD_NAME, globalSaltExists, encryptionGlobalSalt::get);

        // Check if the model was not provided to determine if it is set in the environment.
        checkFieldInitialized(statuses, ENCRYPTION_PASSWORD_FIELD_NAME, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_PWD, passwordExists, encryptionUtility::isPasswordMissing);
        checkFieldInitialized(statuses, ENCRYPTION_GLOBAL_SALT_FIELD_NAME, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_GLOBAL_SALT, globalSaltExists, encryptionUtility::isGlobalSaltMissing);

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }

    private void checkFieldInitialized(Set<AlertFieldStatus> statuses, String fieldName, String fieldErrorMessage, boolean doesFieldExistInModel, BooleanSupplier isFieldMissing) {
        boolean encryptionInitialized = encryptionUtility.isInitialized();
        if (!doesFieldExistInModel && !encryptionInitialized) {
            boolean encryptionError = addSystemMessageForError(fieldErrorMessage, SystemMessageSeverity.ERROR, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR,
                isFieldMissing.getAsBoolean());
            if (encryptionError) {
                logger.error(fieldErrorMessage);
                statuses.add(AlertFieldStatus.error(fieldName, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
            }
        }
    }

    private void minimumEncryptionFieldLength(Set<AlertFieldStatus> statuses, String fieldName, boolean doesFieldExistInModel, Supplier<String> fieldValueSupplier) {
        if (doesFieldExistInModel && fieldValueSupplier.get().length() < 8) {
            statuses.add(AlertFieldStatus.error(fieldName, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_FIELD_TOO_SHORT));
        }
    }
}
