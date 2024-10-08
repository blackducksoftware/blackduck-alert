package com.blackduck.integration.alert.component.settings.encryption.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class SettingsEncryptionValidatorTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

    private final SystemMessageAccessor systemMessageAccessor = Mockito.mock(SystemMessageAccessor.class);

    @Test
    void validateTest() {
        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtility, systemMessageAccessor);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel("password", Boolean.FALSE, "globalSalt", Boolean.FALSE, false);

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    void validateNotInitializedTest() {
        MockAlertProperties alertPropertiesNoEncryption = new MockAlertProperties();
        alertPropertiesNoEncryption.setEncryptionPassword("");
        alertPropertiesNoEncryption.setEncryptionSalt("");
        FilePersistenceUtil filePersistenceUtilWithoutProperties = new FilePersistenceUtil(alertPropertiesNoEncryption, gson);
        EncryptionUtility encryptionUtilityWithoutProperties = new EncryptionUtility(alertPropertiesNoEncryption, filePersistenceUtilWithoutProperties);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel("password", Boolean.FALSE, "globalSalt", Boolean.FALSE, false);

        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtilityWithoutProperties, systemMessageAccessor);

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    void validateNotInitializedNoPasswordTest() {
        MockAlertProperties alertPropertiesNoEncryption = new MockAlertProperties();
        alertPropertiesNoEncryption.setEncryptionPassword("");
        alertPropertiesNoEncryption.setEncryptionSalt("");
        FilePersistenceUtil filePersistenceUtilWithoutProperties = new FilePersistenceUtil(alertPropertiesNoEncryption, gson);
        EncryptionUtility encryptionUtilityWithoutProperties = new EncryptionUtility(alertPropertiesNoEncryption, filePersistenceUtilWithoutProperties);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel(null, Boolean.FALSE, "globalSalt", Boolean.FALSE, false);

        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtilityWithoutProperties, systemMessageAccessor);

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
    }

    @Test
    void validateNotInitializedNoGlobalSaltTest() {
        MockAlertProperties alertPropertiesNoEncryption = new MockAlertProperties();
        alertPropertiesNoEncryption.setEncryptionPassword("");
        alertPropertiesNoEncryption.setEncryptionSalt("");
        FilePersistenceUtil filePersistenceUtilWithoutProperties = new FilePersistenceUtil(alertPropertiesNoEncryption, gson);
        EncryptionUtility encryptionUtilityWithoutProperties = new EncryptionUtility(alertPropertiesNoEncryption, filePersistenceUtilWithoutProperties);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel("password", Boolean.FALSE, null, Boolean.FALSE, false);
        settingsEncryptionModel.setEncryptionPassword("password");

        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtilityWithoutProperties, systemMessageAccessor);

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
    }

    @Test
    void fieldNameTooShortTest() {
        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtility, systemMessageAccessor);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel("too", Boolean.FALSE, "short", Boolean.FALSE, false);

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(2, validationResponseModel.getErrors().size());
    }
}
