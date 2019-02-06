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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.context.NoTestActionApi;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.api.user.UserModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;

@Component
public class SettingsDescriptorActionApi extends NoTestActionApi {
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
    public FieldModel readConfig(final FieldModel fieldModel) {
        final Optional<UserModel> defaultUser = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER);
        final FieldModel newModel = createFieldModelCopy(fieldModel);
        final boolean defaultUserPasswordSet = defaultUser.map(UserModel::getPassword).filter(StringUtils::isNotBlank).isPresent();
        newModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(null, defaultUserPasswordSet));
        newModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(null, encryptionUtility.isPasswordSet()));
        newModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(null, encryptionUtility.isPasswordSet()));
        return newModel;
    }

    @Override
    public FieldModel updateConfig(final FieldModel fieldModel) {
        return saveConfig(fieldModel);
    }

    @Override
    public FieldModel saveConfig(final FieldModel fieldModel) {
        saveDefaultAdminUserPassword(fieldModel);
        saveDefaultAdminUserEmail(fieldModel);
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

        return new FieldModel(fieldModel.getDescriptorName(), fieldModel.getContext(), fields);
    }

    private void saveDefaultAdminUserPassword(final FieldModel fieldModel) {
        final Optional<FieldValueModel> optionalPassword = fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD);
        if (optionalPassword.isPresent()) {
            final String password = optionalPassword.flatMap(FieldValueModel::getValue).orElse("");
            if (StringUtils.isNotBlank(password)) {
                userAccessor.changeUserPassword(UserAccessor.DEFAULT_ADMIN_USER, password);
            }
        }
    }

    private void saveDefaultAdminUserEmail(final FieldModel fieldModel) {
        final Optional<FieldValueModel> optionalEmail = fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL);
        if (optionalEmail.isPresent()) {
            userAccessor.changeUserEmailAddress(UserAccessor.DEFAULT_ADMIN_USER, optionalEmail.flatMap(FieldValueModel::getValue).orElse(""));
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
