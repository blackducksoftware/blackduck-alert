/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;

@Component
public class AuthenticationConfigurationValidator implements GlobalConfigurationValidator {
    private static final String SAML_LDAP_ENABLED_ERROR = "Can't enable both SAML and LDAP authentication";

    private final FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public AuthenticationConfigurationValidator(FilePersistenceUtil filePersistenceUtil) {
        this.filePersistenceUtil = filePersistenceUtil;
    }

    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        Set<AlertFieldStatus> validationResults = new HashSet<>();

        Boolean ldapEnabled = fieldModel.getFieldValue(AuthenticationDescriptor.KEY_LDAP_ENABLED).map(Boolean::valueOf).orElse(false);
        Boolean samlEnabled = fieldModel.getFieldValue(AuthenticationDescriptor.KEY_SAML_ENABLED).map(Boolean::valueOf).orElse(false);

        if (ldapEnabled && samlEnabled) {
            validationResults.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, SAML_LDAP_ENABLED_ERROR));
            validationResults.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_ENABLED, SAML_LDAP_ENABLED_ERROR));
        }

        ConfigurationFieldValidator configurationFieldValidator = new ConfigurationFieldValidator(fieldModel);
        if (ldapEnabled) {
            validationResults.addAll(validateLdapConfiguration(fieldModel));
        }

        if (samlEnabled) {
            validationResults.addAll(validateSamlConfiguration(fieldModel));
        }

        return validationResults;
    }

    private List<AlertFieldStatus> validateLdapConfiguration(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = new ConfigurationFieldValidator(fieldModel);
        return configurationFieldValidator.containsRequiredFields(List.of(
            AuthenticationDescriptor.KEY_LDAP_SERVER,
            AuthenticationDescriptor.KEY_LDAP_MANAGER_DN,
            AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD
        ));
    }

    private List<AlertFieldStatus> validateSamlConfiguration(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = new ConfigurationFieldValidator(fieldModel);
        List<AlertFieldStatus> requiredFieldStatuses = configurationFieldValidator.containsRequiredFields(List.of(
            AuthenticationDescriptor.KEY_SAML_ENTITY_ID,
            AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL
        ));

        validateMetaDataFile(fieldModel).ifPresent(requiredFieldStatuses::add);
        validateMetaDataUrl(fieldModel, AuthenticationDescriptor.KEY_SAML_METADATA_URL).ifPresent(requiredFieldStatuses::add);
        validateMetaDataUrl(fieldModel, AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL).ifPresent(requiredFieldStatuses::add);

        return requiredFieldStatuses;
    }

    private Optional<AlertFieldStatus> validateMetaDataUrl(FieldModel fieldModel, String fieldKey) {
        boolean fieldHasValues = fieldModel.getFieldValueModel(fieldKey).map(FieldValueModel::hasValues).orElse(false);
        if (!fieldHasValues && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE)) {
            return Optional.of(AlertFieldStatus.error(fieldKey, AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_URL_MISSING));
        }
        return Optional.empty();
    }

    private Optional<AlertFieldStatus> validateMetaDataFile(FieldModel fieldModel) {
        boolean metadataUrlEmpty = fieldModel.getFieldValueModel(AuthenticationDescriptor.KEY_SAML_METADATA_URL)
                                       .map(field -> !field.hasValues()).orElse(true);
        if (metadataUrlEmpty && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE)) {
            return Optional.of(AlertFieldStatus.error(AuthenticationDescriptor.SAML_METADATA_FILE, AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_FILE_MISSING));
        }
        return Optional.empty();
    }
}
