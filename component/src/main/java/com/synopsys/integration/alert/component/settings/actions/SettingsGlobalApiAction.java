/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.validator.SettingsSystemValidator;

@Component
public class SettingsGlobalApiAction extends ApiAction {
    private final Logger logger = LoggerFactory.getLogger(SettingsGlobalApiAction.class);
    private final EncryptionUtility encryptionUtility;
    private final SettingsSystemValidator settingsSystemValidator;

    @Autowired
    public SettingsGlobalApiAction(EncryptionUtility encryptionUtility, SettingsSystemValidator settingsSystemValidator) {
        this.encryptionUtility = encryptionUtility;
        this.settingsSystemValidator = settingsSystemValidator;
    }

    @Override
    public FieldModel afterGetAction(FieldModel fieldModel) {
        FieldModel fieldModelCopy = createFieldModelCopy(fieldModel);
        fieldModelCopy.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(null, encryptionUtility.isPasswordSet()));
        fieldModelCopy.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(null, encryptionUtility.isGlobalSaltSet()));
        return fieldModelCopy;
    }

    @Override
    public FieldModel beforeSaveAction(FieldModel fieldModel) {
        return handleNewAndUpdatedConfig(fieldModel);
    }

    @Override
    public FieldModel beforeUpdateAction(FieldModel fieldModel) {
        return handleNewAndUpdatedConfig(fieldModel);
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        handleAfterNewAndUpdate();
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) throws AlertException {
        handleAfterNewAndUpdate();
        return super.afterUpdateAction(previousFieldModel, currentFieldModel);
    }

    private FieldModel createFieldModelCopy(FieldModel fieldModel) {
        HashMap<String, FieldValueModel> fields = new HashMap<>();
        fields.putAll(fieldModel.getKeyToValues());

        FieldModel modelToSave = new FieldModel(fieldModel.getDescriptorName(), fieldModel.getContext(),
            fieldModel.getCreatedAt(), fieldModel.getLastUpdated(), fields);
        modelToSave.setId(fieldModel.getId());
        return modelToSave;
    }

    private FieldModel handleNewAndUpdatedConfig(FieldModel fieldModel) {
        saveEncryptionProperties(fieldModel);
        return scrubModel(fieldModel);
    }

    private void handleAfterNewAndUpdate() {
        settingsSystemValidator.validateEncryption();
    }

    private FieldModel scrubModel(FieldModel fieldModel) {
        Map<String, FieldValueModel> keyToValues = new HashMap<>(fieldModel.getKeyToValues());
        keyToValues.remove(SettingsDescriptor.KEY_ENCRYPTION_PWD);
        keyToValues.remove(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

        return new FieldModel(fieldModel.getDescriptorName(), fieldModel.getContext(), fieldModel.getCreatedAt(), fieldModel.getLastUpdated(), keyToValues);
    }

    private void saveEncryptionProperties(FieldModel fieldModel) {
        try {
            Optional<FieldValueModel> optionalEncryptionPassword = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD);
            Optional<FieldValueModel> optionalEncryptionSalt = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

            if (optionalEncryptionPassword.isPresent()) {
                String passwordToSave = optionalEncryptionPassword.get().getValue().orElse("");
                if (StringUtils.isNotBlank(passwordToSave)) {
                    encryptionUtility.updatePasswordFieldInVolumeDataFile(passwordToSave);
                }
            }

            if (optionalEncryptionSalt.isPresent()) {
                String saltToSave = optionalEncryptionSalt.get().getValue().orElse("");
                if (StringUtils.isNotBlank(saltToSave)) {
                    encryptionUtility.updateSaltFieldInVolumeDataFile(saltToSave);
                }
            }
        } catch (IllegalArgumentException | IOException ex) {
            logger.error("Error saving encryption configuration.", ex);
        }
    }

}
