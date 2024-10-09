package com.blackduck.integration.alert.component.settings.encryption.action;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.blackduck.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;
import com.blackduck.integration.alert.component.settings.encryption.validator.SettingsEncryptionValidator;

@Component
public class SettingsEncryptionCrudActions {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConfigurationCrudHelper configurationHelper;
    private final EncryptionUtility encryptionUtility;
    private final SettingsEncryptionValidator validator;

    @Autowired
    public SettingsEncryptionCrudActions(
        AuthorizationManager authorizationManager,
        EncryptionUtility encryptionUtility,
        SettingsEncryptionValidator validator,
        SettingsDescriptorKey settingsDescriptorKey
    ) {
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
        // Due to the unique behavior of Encryption, in the event that nothing is saved on the system through the environment nor volume data file, we must supply
        //  a value of TRUE for the existingModelSupplier. The encryptionUtility handles writing the data correctly whether the model exists or not. Without this,
        //  the value will not be written to the volume data file during the update.
        return configurationHelper.update(
            () -> validator.validate(requestResource),
            () -> Boolean.TRUE,
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
        return new SettingsEncryptionModel(
            encryptionUtility.isPasswordSet(),
            encryptionUtility.isGlobalSaltSet(),
            encryptionUtility.isEncryptionFromEnvironment()
        );
    }
}
