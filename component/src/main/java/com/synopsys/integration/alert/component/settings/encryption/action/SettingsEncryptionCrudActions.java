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
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;
import com.synopsys.integration.alert.component.settings.encryption.validator.SettingsEncryptionValidator;

@Component
public class SettingsEncryptionCrudActions {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    //Encryption is not stored in the database and there will ever only be one ID associated with it.
    private static final Long DEFAULT_ENCRYPTION_ID = 1L;

    private final ConfigurationCrudHelper configurationHelper;
    private final EncryptionUtility encryptionUtility;
    private final SettingsEncryptionValidator validator;

    @Autowired
    public SettingsEncryptionCrudActions(AuthorizationManager authorizationManager, EncryptionUtility encryptionUtility, SettingsEncryptionValidator validator) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, new SettingsDescriptorKey());
        this.encryptionUtility = encryptionUtility;
        this.validator = validator;
    }

    public ActionResponse<SettingsEncryptionModel> getOne(Long id) {
        return configurationHelper.getOne(
            () -> getSettingsEncryptionModel(id)
        );
    }

    public ActionResponse<SettingsEncryptionModel> create(SettingsEncryptionModel resource) {
        return configurationHelper.create(
            () -> validator.validate(resource),
            () -> createSettingsEncryptionModel(resource)
        );
    }

    public ActionResponse<SettingsEncryptionModel> update(Long id, SettingsEncryptionModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource),
            () -> getSettingsEncryptionModel(id).isPresent(),
            () -> updateSettingsEncryptionModel(requestResource)
        );
    }

    private Optional<SettingsEncryptionModel> getSettingsEncryptionModel(Long id) {
        if (!id.equals(DEFAULT_ENCRYPTION_ID)) {
            return Optional.empty();
        }

        if (encryptionUtility.isInitialized()) {
            return Optional.of(createMaskedSettingsEncryptionModel());
        }
        return Optional.empty();
    }

    private SettingsEncryptionModel createSettingsEncryptionModel(SettingsEncryptionModel model) {
        //  We always want encryption settings to be set in the environment. The only time they could be empty is when the system is initialized.
        //  Since we do not store encryption settings in the database, create and update are the same.
        try {
            Optional<String> optionalEncryptionPassword = model.getPassword();
            Optional<String> optionalEncryptionSalt = model.getGlobalSalt();

            if (optionalEncryptionPassword.isPresent()) {
                String passwordToSave = optionalEncryptionPassword.get();
                if (StringUtils.isNotBlank(passwordToSave)) {
                    encryptionUtility.updatePasswordField(passwordToSave);
                }
            }

            if (optionalEncryptionSalt.isPresent()) {
                String saltToSave = optionalEncryptionSalt.get();
                if (StringUtils.isNotBlank(saltToSave)) {
                    encryptionUtility.updateSaltField(saltToSave);
                }
            }
        } catch (IllegalArgumentException | IOException ex) {
            logger.error("Error saving encryption configuration.", ex);
        }
        return createMaskedSettingsEncryptionModel();
    }

    private SettingsEncryptionModel updateSettingsEncryptionModel(SettingsEncryptionModel model) {
        return createSettingsEncryptionModel(model);
    }

    private SettingsEncryptionModel createMaskedSettingsEncryptionModel() {
        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel();
        settingsEncryptionModel.setPassword(AlertRestConstants.PASSWORD_FIELD_MASK);
        settingsEncryptionModel.setGlobalSalt(AlertRestConstants.PASSWORD_FIELD_MASK);
        return settingsEncryptionModel;
    }
}
