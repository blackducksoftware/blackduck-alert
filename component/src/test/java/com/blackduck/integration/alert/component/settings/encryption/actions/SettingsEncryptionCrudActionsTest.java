package com.blackduck.integration.alert.component.settings.encryption.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.blackduck.integration.alert.component.settings.encryption.action.SettingsEncryptionCrudActions;
import com.blackduck.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;
import com.blackduck.integration.alert.component.settings.encryption.validator.SettingsEncryptionValidator;
import com.synopsys.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class SettingsEncryptionCrudActionsTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

    private final AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
    private final DescriptorKey descriptorKey = new SettingsDescriptorKey();
    private final PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    private final Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
    private final AuthorizationManager authorizationManager = authenticationTestUtils
        .createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));

    private final SystemMessageAccessor systemMessageAccessor = Mockito.mock(SystemMessageAccessor.class);
    private final SettingsEncryptionValidator validator = new SettingsEncryptionValidator(encryptionUtility, systemMessageAccessor);

    private final SettingsDescriptorKey settingsDescriptorKey = new SettingsDescriptorKey();

    @Test
    void getOneTest() {
        SettingsEncryptionCrudActions configActions = new SettingsEncryptionCrudActions(authorizationManager, encryptionUtility, validator, settingsDescriptorKey);
        ActionResponse<SettingsEncryptionModel> actionResponse = configActions.getOne();

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    void getOneNotInitializedTest() {
        MockAlertProperties alertPropertiesNoEncryption = new MockAlertProperties();
        alertPropertiesNoEncryption.setEncryptionPassword("");
        alertPropertiesNoEncryption.setEncryptionSalt("");
        FilePersistenceUtil filePersistenceUtilWithoutProperties = new FilePersistenceUtil(alertPropertiesNoEncryption, gson);
        EncryptionUtility encryptionUtilityWithoutProperties = new EncryptionUtility(alertPropertiesNoEncryption, filePersistenceUtilWithoutProperties);

        SettingsEncryptionCrudActions configActions = new SettingsEncryptionCrudActions(authorizationManager, encryptionUtilityWithoutProperties, validator, settingsDescriptorKey);
        ActionResponse<SettingsEncryptionModel> actionResponse = configActions.getOne();

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, actionResponse.getHttpStatus());
    }

    @Test
    void updateTest() {
        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel("password", Boolean.FALSE, "globalSalt", Boolean.FALSE, false);

        SettingsEncryptionCrudActions configActions = new SettingsEncryptionCrudActions(authorizationManager, encryptionUtility, validator, settingsDescriptorKey);
        ActionResponse<SettingsEncryptionModel> actionResponse = configActions.update(settingsEncryptionModel);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    private void assertModelObfuscated(ActionResponse<SettingsEncryptionModel> actionResponse) {
        Optional<SettingsEncryptionModel> optionalSettingsEncryptionModel = actionResponse.getContent();
        assertTrue(optionalSettingsEncryptionModel.isPresent());

        SettingsEncryptionModel settingsEncryptionModel = optionalSettingsEncryptionModel.get();
        assertTrue(settingsEncryptionModel.getEncryptionPassword().isEmpty());
        assertTrue(settingsEncryptionModel.getEncryptionGlobalSalt().isEmpty());
        assertTrue(settingsEncryptionModel.getIsEncryptionPasswordSet());
        assertTrue(settingsEncryptionModel.getIsEncryptionGlobalSaltSet());

        assertTrue(settingsEncryptionModel.isReadOnly());
    }
}
