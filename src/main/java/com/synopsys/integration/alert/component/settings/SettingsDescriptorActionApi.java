/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.component.settings;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.api.user.UserModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.alert.web.model.configuration.TestConfigModel;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class SettingsDescriptorActionApi extends DescriptorActionApi {
    private static final Logger logger = LoggerFactory.getLogger(SettingsDescriptorActionApi.class);
    private final EncryptionUtility encryptionUtility;
    private final UserAccessor userAccessor;
    private final SystemValidator systemValidator;

    @Autowired
    public SettingsDescriptorActionApi(final EncryptionUtility encryptionUtility, final UserAccessor userAccessor, final SystemValidator systemValidator) {
        this.encryptionUtility = encryptionUtility;
        this.userAccessor = userAccessor;
        this.systemValidator = systemValidator;
    }

    @Override
    public void validateConfig(final Collection<ConfigField> descriptorFields, final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        super.validateConfig(descriptorFields, fieldModel, fieldErrors);
        validateLDAPSettings(fieldModel, fieldErrors);
    }

    @Override
    public void testConfig(final Collection<ConfigField> descriptorFields, final TestConfigModel testConfig) throws IntegrationException {

    }

    @Override
    public FieldModel readConfig(final FieldModel fieldModel) {
        final Optional<UserModel> defaultUser = userAccessor.getUser("sysadmin");
        final FieldModel newModel = createFieldModelCopy(fieldModel);
        final boolean defaultUserPasswordSet = defaultUser.isPresent() && StringUtils.isNotBlank(defaultUser.get().getPassword());
        newModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(null, defaultUserPasswordSet));
        newModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(null, encryptionUtility.isPasswordSet()));
        newModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(null, encryptionUtility.isPasswordSet()));
        return newModel;
    }

    @Override
    public FieldModel updateConfig(final FieldModel fieldModel) {
        saveDefaultAdminUserPassword(fieldModel);
        saveEncryptionProperties(fieldModel);
        return createScrubbedModel(fieldModel);
    }

    @Override
    public FieldModel saveConfig(final FieldModel fieldModel) {
        saveDefaultAdminUserPassword(fieldModel);
        saveEncryptionProperties(fieldModel);
        systemValidator.validate();
        return createScrubbedModel(fieldModel);
    }

    private void validateLDAPSettings(final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        final Optional<FieldValueModel> ldapEnabled = fieldModel.getField(SettingsDescriptor.KEY_LDAP_ENABLED);
        if (ldapEnabled.isPresent()) {
            final Boolean isLdapEnabled = Boolean.valueOf(ldapEnabled.get().getValue().orElse("false"));
            if (isLdapEnabled) {
                validateRequiredField(SettingsDescriptor.KEY_LDAP_SERVER, fieldModel, fieldErrors, SettingsDescriptor.FIELD_ERROR_LDAP_SERVER_MISSING, (valueModel) -> {
                    if (StringUtils.isBlank(valueModel.getValue().orElse(""))) {
                        fieldErrors.put(SettingsDescriptor.KEY_LDAP_SERVER, SettingsDescriptor.FIELD_ERROR_LDAP_SERVER_MISSING);
                    }
                    return null;
                });
            }
        }
    }

    private void validateRequiredField(final String fieldKey, final FieldModel fieldModel, final Map<String, String> fieldErrors, final String fieldMissingMessage, final Function<FieldValueModel, Void> validationFunction) {
        if (isFieldMissing(fieldKey, fieldModel)) {
            fieldErrors.put(fieldKey, fieldMissingMessage);
        } else {
            validateField(fieldKey, fieldModel, validationFunction);
        }
    }

    private boolean isFieldMissing(final String key, final FieldModel fieldModel) {
        return fieldModel.getField(key).isEmpty();
    }

    private void validateField(final String fieldKey, final FieldModel fieldModel, final Function<FieldValueModel, Void> validationFunction) {
        final Optional<FieldValueModel> optionalField = fieldModel.getField(fieldKey);
        if (optionalField.isPresent()) {
            final FieldValueModel valueModel = optionalField.get();
            final boolean validateField = !valueModel.isSet() || valueModel.hasValues();
            if (validateField) {
                validationFunction.apply(valueModel);
            }
        }
    }

    private FieldModel createScrubbedModel(final FieldModel fieldModel) {
        final HashMap<String, FieldValueModel> fields = new HashMap<>();
        fields.putAll(fieldModel.getKeyToValues());
        fields.remove(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD);
        fields.remove(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD);
        fields.remove(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

        final FieldModel modelToSave = new FieldModel(fieldModel.getDescriptorName(), fieldModel.getContext(), fields);
        return modelToSave;
    }

    private void saveDefaultAdminUserPassword(final FieldModel fieldModel) {
        final Optional<FieldValueModel> optionalPassword = fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD);
        if (optionalPassword.isPresent()) {
            final String password = optionalPassword.get().getValue().orElse("");
            if (StringUtils.isNotBlank(password)) {
                userAccessor.changeUserPassword(UserAccessor.DEFAULT_ADMIN_USER, password);
            }
        }
    }

    private void saveEncryptionProperties(final FieldModel fieldModel) {
        try {
            final Optional<FieldValueModel> optionalEncryptionPassword = fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD);
            final Optional<FieldValueModel> optionalEncryptionSalt = fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

            if (optionalEncryptionPassword.isPresent()) {
                final String passwordToSave = optionalEncryptionPassword.get().getValue().orElse("");
                if (StringUtils.isNotBlank(passwordToSave)) {
                    encryptionUtility.updatePasswordField(passwordToSave);
                }
            }

            if (optionalEncryptionSalt.isPresent()) {
                final String saltToSave = optionalEncryptionSalt.get().getValue().orElse("");
                if (StringUtils.isNotBlank(saltToSave)) {
                    encryptionUtility.updateSaltField(saltToSave);
                }
            }
        } catch (final IllegalArgumentException | IOException ex) {
            logger.error("Error saving encryption configuration.", ex);
        }
    }
}
