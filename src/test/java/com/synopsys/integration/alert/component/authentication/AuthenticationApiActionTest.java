package com.synopsys.integration.alert.component.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertFieldStatus;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationUIConfig;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.web.config.FieldValidationAction;

public class AuthenticationApiActionTest {

    @Test
    public void testLdapEnabled() {
        FilePersistenceUtil filePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        EncryptionSettingsValidator encryptionValidator = new EncryptionSettingsValidator(encryptionUtility);
        AuthenticationUIConfig authenticationUIConfig = new AuthenticationUIConfig(filePersistenceUtil, encryptionValidator);
        authenticationUIConfig.setConfigFields();
        AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
        FieldModel fieldModel = new FieldModel(authenticationDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(), true));
        fieldModel.putField(AuthenticationDescriptor.KEY_LDAP_ENABLED, new FieldValueModel(List.of("true"), false));
        fieldModel.putField(AuthenticationDescriptor.KEY_LDAP_SERVER, new FieldValueModel(List.of(""), false));
        HashMap<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(authenticationUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertFalse(fieldErrors.isEmpty());
        assertEquals(AuthenticationDescriptor.FIELD_ERROR_LDAP_SERVER_MISSING, fieldErrors.get(AuthenticationDescriptor.KEY_LDAP_SERVER).getFieldMessage());
    }
}
