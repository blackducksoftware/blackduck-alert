package com.synopsys.integration.alert.component.settings.encryption.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

public class SettingsEncryptionValidatorTest {
    private final Gson gson = new Gson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

    private final SystemMessageAccessor systemMessageAccessor = Mockito.mock(SystemMessageAccessor.class);

    @Test
    public void validateTest() {
        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtility, systemMessageAccessor);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel();
        settingsEncryptionModel.setEncryptionPassword("password");
        settingsEncryptionModel.setEncryptionGlobalSalt("globalSalt");

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    public void validateNotInitializedTest() {
        MockAlertProperties alertPropertiesNoEncryption = new MockAlertProperties();
        alertPropertiesNoEncryption.setEncryptionPassword("");
        alertPropertiesNoEncryption.setEncryptionSalt("");
        FilePersistenceUtil filePersistenceUtilWithoutProperties = new FilePersistenceUtil(alertPropertiesNoEncryption, gson);
        EncryptionUtility encryptionUtilityWithoutProperties = new EncryptionUtility(alertPropertiesNoEncryption, filePersistenceUtilWithoutProperties);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel();
        settingsEncryptionModel.setEncryptionPassword("password");
        settingsEncryptionModel.setEncryptionGlobalSalt("globalSalt");

        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtilityWithoutProperties, systemMessageAccessor);

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    public void validateNotInitializedNoPasswordTest() {
        MockAlertProperties alertPropertiesNoEncryption = new MockAlertProperties();
        alertPropertiesNoEncryption.setEncryptionPassword("");
        alertPropertiesNoEncryption.setEncryptionSalt("");
        FilePersistenceUtil filePersistenceUtilWithoutProperties = new FilePersistenceUtil(alertPropertiesNoEncryption, gson);
        EncryptionUtility encryptionUtilityWithoutProperties = new EncryptionUtility(alertPropertiesNoEncryption, filePersistenceUtilWithoutProperties);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel();
        settingsEncryptionModel.setEncryptionGlobalSalt("globalSalt");

        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtilityWithoutProperties, systemMessageAccessor);

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
    }

    @Test
    public void validateNotInitializedNoGlobalSaltTest() {
        MockAlertProperties alertPropertiesNoEncryption = new MockAlertProperties();
        alertPropertiesNoEncryption.setEncryptionPassword("");
        alertPropertiesNoEncryption.setEncryptionSalt("");
        FilePersistenceUtil filePersistenceUtilWithoutProperties = new FilePersistenceUtil(alertPropertiesNoEncryption, gson);
        EncryptionUtility encryptionUtilityWithoutProperties = new EncryptionUtility(alertPropertiesNoEncryption, filePersistenceUtilWithoutProperties);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel();
        settingsEncryptionModel.setEncryptionPassword("password");

        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtilityWithoutProperties, systemMessageAccessor);

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
    }

    @Test
    public void fieldNameTooShortTest() {
        SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtility, systemMessageAccessor);

        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel();
        settingsEncryptionModel.setEncryptionPassword("too");
        settingsEncryptionModel.setEncryptionGlobalSalt("short");

        ValidationResponseModel validationResponseModel = validator.validate(settingsEncryptionModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(2, validationResponseModel.getErrors().size());
    }
}
