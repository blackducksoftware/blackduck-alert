/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.api.authentication.validator.AuthenticationConfigurationFieldModelValidator;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

/**
 * @deprecated This class will be removed in 8.0.0.
 */
@Deprecated(forRemoval = true)
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
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(mockAlertProperties, BlackDuckServicesFactory.createDefaultGson());
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
