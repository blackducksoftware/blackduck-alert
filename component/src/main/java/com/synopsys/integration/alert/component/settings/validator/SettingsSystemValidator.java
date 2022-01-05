/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.system.BaseSystemValidator;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

//TODO: Deprecate this class once the SettingsEncryptionValidator is complete and the old settings page is removed.
@Component
public class SettingsSystemValidator extends BaseSystemValidator {
    private final Logger logger = LoggerFactory.getLogger(SettingsSystemValidator.class);
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public SettingsSystemValidator(EncryptionUtility encryptionUtility, SystemMessageAccessor systemMessageAccessor) {
        super(systemMessageAccessor);
        this.encryptionUtility = encryptionUtility;
    }

    public boolean validateEncryption() {
        getSystemMessageAccessor().removeSystemMessagesByType(SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        boolean valid = true;
        if (!encryptionUtility.isInitialized()) {
            logger.error("Encryption utilities: Not Initialized");

            boolean encryptionError = addSystemMessageForError(SettingsDescriptor.FIELD_ERROR_ENCRYPTION_PWD, SystemMessageSeverity.ERROR, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR,
                encryptionUtility.isPasswordMissing());
            if (encryptionError) {
                valid = false;
                logger.error(SettingsDescriptor.FIELD_ERROR_ENCRYPTION_PWD);
            }
            boolean saltError = addSystemMessageForError(SettingsDescriptor.FIELD_ERROR_ENCRYPTION_GLOBAL_SALT, SystemMessageSeverity.ERROR, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR,
                encryptionUtility.isGlobalSaltMissing());
            if (saltError) {
                valid = false;
                logger.error(SettingsDescriptor.FIELD_ERROR_ENCRYPTION_GLOBAL_SALT);
            }
            return valid;
        }

        logger.info("Encryption utilities: Initialized");
        return valid;
    }

}
