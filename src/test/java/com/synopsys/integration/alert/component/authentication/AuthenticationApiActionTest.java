package com.synopsys.integration.alert.component.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
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
        AuthenticationUIConfig authenticationUIConfig = new AuthenticationUIConfig(filePersistenceUtil);
        AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
        final FieldModel fieldModel = new FieldModel(authenticationDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(), true));
        fieldModel.putField(AuthenticationDescriptor.KEY_LDAP_ENABLED, new FieldValueModel(List.of("true"), false));
        fieldModel.putField(AuthenticationDescriptor.KEY_LDAP_SERVER, new FieldValueModel(List.of(""), false));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = DataStructureUtils.convertToMapWithCopiedValue(authenticationUIConfig.createFields(), ConfigField::getKey);
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertFalse(fieldErrors.isEmpty());
        assertEquals(AuthenticationDescriptor.FIELD_ERROR_LDAP_SERVER_MISSING, fieldErrors.get(AuthenticationDescriptor.KEY_LDAP_SERVER));
    }
}
