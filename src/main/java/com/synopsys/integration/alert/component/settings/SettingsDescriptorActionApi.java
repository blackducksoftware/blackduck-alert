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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.api.user.UserModel;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
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
    public void validateConfig(final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors) {
        final String defaultUserPassword = fieldAccessor.getString(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD).orElse(null);
        final String encryptionPassword = fieldAccessor.getString(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD).orElse(null);
        final String encryptionSalt = fieldAccessor.getString(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).orElse(null);

        if (StringUtils.isBlank(defaultUserPassword)) {
            fieldErrors.put(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_PASSWORD);
        }

        if (StringUtils.isBlank(encryptionPassword)) {
            fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_PASSWORD);
        }

        if (StringUtils.isBlank(encryptionSalt)) {
            fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_GLOBAL_SALT);
        }
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {

    }

    @Override
    public void readConfig(final FieldModel fieldModel) {
        final Optional<UserModel> defaultUser = userAccessor.getUser("sysadmin");
        final boolean defaultUserPasswordSet = defaultUser.isPresent() && StringUtils.isNotBlank(defaultUser.get().getPassword());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(List.of(""), defaultUserPasswordSet));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(List.of(""), encryptionUtility.isPasswordSet()));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(""), encryptionUtility.isPasswordSet()));
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
        final String password = fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD).getValue().orElse("");
        if (StringUtils.isNotBlank(password)) {
            userAccessor.changeUserPassword(UserAccessor.DEFAULT_ADMIN_USER, password);
        }
    }

    private void saveEncryptionProperties(final FieldModel fieldModel) {
        try {
            final String passwordToSave = fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD).getValue().orElse("");
            final String saltToSave = fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).getValue().orElse("");
            if (StringUtils.isNotBlank(passwordToSave)) {
                encryptionUtility.updatePasswordField(passwordToSave);
            }

            if (StringUtils.isNotBlank(saltToSave)) {
                encryptionUtility.updateSaltField(saltToSave);
            }
        } catch (final IllegalArgumentException | IOException ex) {
            logger.error("Error saving encryption configuration.", ex);
        }
    }
}
