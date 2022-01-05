/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.encryption.action;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;
import com.synopsys.integration.alert.component.settings.encryption.validator.SettingsEncryptionValidator;

@Component
public class SettingsEncryptionCrudActions {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConfigurationCrudHelper configurationHelper;
    private final EncryptionUtility encryptionUtility;
    private final SettingsEncryptionValidator validator;

    @Autowired
    public SettingsEncryptionCrudActions(AuthorizationManager authorizationManager, EncryptionUtility encryptionUtility, SettingsEncryptionValidator validator, SettingsDescriptorKey settingsDescriptorKey) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, settingsDescriptorKey);
        this.encryptionUtility = encryptionUtility;
        this.validator = validator;
    }

    public ActionResponse<SettingsEncryptionModel> getOne() {
        return configurationHelper.getOne(
            this::getSettingsEncryptionModel
        );
    }

    public ActionResponse<SettingsEncryptionModel> update(SettingsEncryptionModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource),
            () -> getSettingsEncryptionModel().isPresent(),
            () -> updateSettingsEncryptionModel(requestResource)
        );
    }

    private Optional<SettingsEncryptionModel> getSettingsEncryptionModel() {
        if (encryptionUtility.isInitialized()) {
            return Optional.of(createMaskedSettingsEncryptionModel());
        }
        return Optional.empty();
    }

    private SettingsEncryptionModel updateSettingsEncryptionModel(SettingsEncryptionModel model) {
        try {
            Optional<String> optionalEncryptionPassword = model.getEncryptionPassword();
            Optional<String> optionalEncryptionSalt = model.getEncryptionGlobalSalt();

            if (optionalEncryptionPassword.isPresent()) {
                String passwordToSave = optionalEncryptionPassword.get();
                if (StringUtils.isNotBlank(passwordToSave)) {
                    encryptionUtility.updatePasswordFieldInVolumeDataFile(passwordToSave);
                }
            }

            if (optionalEncryptionSalt.isPresent()) {
                String saltToSave = optionalEncryptionSalt.get();
                if (StringUtils.isNotBlank(saltToSave)) {
                    encryptionUtility.updateSaltFieldInVolumeDataFile(saltToSave);
                }
            }
        } catch (IllegalArgumentException | IOException ex) {
            logger.error("Error saving encryption configuration.", ex);
        }
        return createMaskedSettingsEncryptionModel();
    }

    private SettingsEncryptionModel createMaskedSettingsEncryptionModel() {
        // EncryptionUtility does not return a model. A SettingsEncryptionModel with values must be created in order to obfuscate in the ConfigurationCrudHelper later.
        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel();
        settingsEncryptionModel.setIsEncryptionPasswordSet(true);
        settingsEncryptionModel.setIsEncryptionGlobalSaltSet(true);
        settingsEncryptionModel.setReadOnly(encryptionUtility.isEncryptionFromEnvironment());
        return settingsEncryptionModel;
    }
}
