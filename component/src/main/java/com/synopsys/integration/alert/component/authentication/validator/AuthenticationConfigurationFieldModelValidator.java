/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.validator;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;

@Component
public class AuthenticationConfigurationFieldModelValidator implements GlobalConfigurationFieldModelValidator {
    private static final String SAML_LDAP_ENABLED_ERROR = "Can't enable both SAML and LDAP authentication";

    private final FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public AuthenticationConfigurationFieldModelValidator(FilePersistenceUtil filePersistenceUtil) {
        this.filePersistenceUtil = filePersistenceUtil;
    }

    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        boolean ldapEnabled = configurationFieldValidator.getBooleanValue(AuthenticationDescriptor.KEY_LDAP_ENABLED).orElse(false);
        boolean samlEnabled = configurationFieldValidator.getBooleanValue(AuthenticationDescriptor.KEY_SAML_ENABLED).orElse(false);

        if (ldapEnabled && samlEnabled) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, SAML_LDAP_ENABLED_ERROR));
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_ENABLED, SAML_LDAP_ENABLED_ERROR));
        }

        if (ldapEnabled) {
            configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
                AuthenticationDescriptor.KEY_LDAP_SERVER,
                AuthenticationDescriptor.KEY_LDAP_MANAGER_DN,
                AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD
            ));
        }

        if (samlEnabled) {
            configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
                AuthenticationDescriptor.KEY_SAML_ENTITY_ID,
                AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL
            ));

            if (configurationFieldValidator.fieldHasNoReadableValue(AuthenticationDescriptor.KEY_SAML_METADATA_URL) && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE)) {
                configurationFieldValidator.addValidationResults(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_METADATA_FILE, AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_FILE_MISSING));
            }

            validateMetaData(configurationFieldValidator, AuthenticationDescriptor.KEY_SAML_METADATA_URL, AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_URL_MISSING);
            validateMetaData(configurationFieldValidator, AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL, AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_URL_MISSING);
        }

        return configurationFieldValidator.getValidationResults();
    }

    private void validateMetaData(ConfigurationFieldValidator configurationFieldValidator, String fieldKey, String message) {
        if (configurationFieldValidator.fieldHasNoReadableValue(fieldKey) && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE)) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(fieldKey, message));
        }
    }

}
