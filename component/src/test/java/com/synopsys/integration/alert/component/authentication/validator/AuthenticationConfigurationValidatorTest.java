package com.synopsys.integration.alert.component.authentication.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;

public class AuthenticationConfigurationValidatorTest {

    /*
     * LDAP Enabled: Not allowed with SAML Enabled
     * LDAP Server: required with LDAP Enabled
     * LDAP Manager Dn: required with LDAP Enabled
     * LDAP Manager Password: required with LDAP Enabled
     *
     * LDAP authentication type: has option
     * LDAP referral: has option
     */

    /*
     * SAML Enabled: Not allowed with LDAP Enabled
     * SAML force auth: required with SAML Enabled
     * SAML Entity ID: required with SAML Enabled
     * SAML Entity base url: required with SAML Enabled, required without SAML metadata file
     * SAML Metadata url: required without SAML metadata file
     * SAML metadata file: required without SAML metadata url
     */

    @Test
    public void verifyValidConfiguration() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter(Map.of());
        validatorAsserter.assertValid();
    }

    @Test
    public void verifyInvalidLdapAndSamlEnabled() {
        Map<String, FieldValueModel> keyToValues = Map.of(
            AuthenticationDescriptor.KEY_LDAP_ENABLED, new FieldValueModel(List.of("true"), true),
            AuthenticationDescriptor.KEY_SAML_ENABLED, new FieldValueModel(List.of("true"), true)
        );
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter(keyToValues);
        validatorAsserter.assertCustom(alertFieldStatuses -> assertTrue(alertFieldStatuses.size() >= 2, alertFieldStatuses.toString()));
    }

    @Test
    public void verifyValidLdapConfiguration() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter(createValidLdapValues());
        validatorAsserter.assertValid();
    }

    @Test
    public void verifyValidSamlConfiguration() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter(createValidSamlValues());
        validatorAsserter.assertValid();
    }

    @Test
    public void missingLdapFields() {
        Map<String, FieldValueModel> keyToValues = Map.of(
            AuthenticationDescriptor.KEY_LDAP_ENABLED, new FieldValueModel(List.of("true"), true)
        );
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter(keyToValues);
        validatorAsserter.assertCustom(alertFieldStatuses -> assertEquals(3, alertFieldStatuses.size(), alertFieldStatuses.toString()));
    }

    @Test
    public void missingSamlFields() {
        Map<String, FieldValueModel> keyToValues = Map.of(
            AuthenticationDescriptor.KEY_SAML_ENABLED, new FieldValueModel(List.of("true"), true)
        );
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter(keyToValues);
        validatorAsserter.assertCustom(alertFieldStatuses -> assertEquals(5, alertFieldStatuses.size(), alertFieldStatuses.toString()));
    }

    @Test
    public void hasSamlMetadataFile() {
        Map<String, FieldValueModel> keyToValues = createValidSamlValues();
        keyToValues.remove(AuthenticationDescriptor.KEY_SAML_METADATA_URL);
        keyToValues.remove(AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL);

        FilePersistenceUtil filePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
        Mockito.when(filePersistenceUtil.uploadFileExists(Mockito.anyString())).thenReturn(true);

        AuthenticationConfigurationFieldModelValidator authenticationConfigurationValidator = new AuthenticationConfigurationFieldModelValidator(filePersistenceUtil);
        Set<AlertFieldStatus> alertFieldStatuses = authenticationConfigurationValidator.validate(new FieldModel(new AuthenticationDescriptorKey().getUniversalKey(), ConfigContextEnum.GLOBAL.name(), keyToValues));

        assertEquals(1, alertFieldStatuses.size());
    }

    private GlobalConfigurationValidatorAsserter createValidatorAsserter(Map<String, FieldValueModel> keyToValues) {
        AlertProperties mockAlertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(mockAlertProperties, new Gson());
        return new GlobalConfigurationValidatorAsserter(new AuthenticationDescriptorKey().getUniversalKey(), new AuthenticationConfigurationFieldModelValidator(filePersistenceUtil), keyToValues);
    }

    private Map<String, FieldValueModel> createValidLdapValues() {
        FieldValueModel ldapEnabled = new FieldValueModel(List.of("true"), true);
        FieldValueModel server = new FieldValueModel(List.of("server"), true);
        FieldValueModel managerDn = new FieldValueModel(List.of("managerDn"), true);
        FieldValueModel managerPassword = new FieldValueModel(List.of("managerPassword"), true);

        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(AuthenticationDescriptor.KEY_LDAP_ENABLED, ldapEnabled);
        keyToValues.put(AuthenticationDescriptor.KEY_LDAP_SERVER, server);
        keyToValues.put(AuthenticationDescriptor.KEY_LDAP_MANAGER_DN, managerDn);
        keyToValues.put(AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD, managerPassword);

        return keyToValues;
    }

    private Map<String, FieldValueModel> createValidSamlValues() {
        FieldValueModel samlEnabled = new FieldValueModel(List.of("true"), true);
        FieldValueModel forceAuth = new FieldValueModel(List.of("true"), true);
        FieldValueModel entityId = new FieldValueModel(List.of("entityId"), true);
        FieldValueModel entityBaseUrl = new FieldValueModel(List.of("entityBaseUrl"), true);
        FieldValueModel metadataUrl = new FieldValueModel(List.of("metadataUrl"), true);

        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(AuthenticationDescriptor.KEY_SAML_ENABLED, samlEnabled);
        keyToValues.put(AuthenticationDescriptor.KEY_SAML_FORCE_AUTH, forceAuth);
        keyToValues.put(AuthenticationDescriptor.KEY_SAML_ENTITY_ID, entityId);
        keyToValues.put(AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL, entityBaseUrl);
        keyToValues.put(AuthenticationDescriptor.KEY_SAML_METADATA_URL, metadataUrl);

        return keyToValues;
    }
}
