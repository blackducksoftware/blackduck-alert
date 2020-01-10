/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.component.settings.actions;

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

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.settings.SettingsValidator;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

@Component
public class SettingsGlobalApiAction extends ApiAction {
    private static final Logger logger = LoggerFactory.getLogger(SettingsGlobalApiAction.class);
    private final EncryptionUtility encryptionUtility;
    private final UserAccessor userAccessor;
    private SettingsValidator settingsValidator;

    @Autowired
    public SettingsGlobalApiAction(EncryptionUtility encryptionUtility, UserAccessor userAccessor, SettingsValidator settingsValidator) {
        this.encryptionUtility = encryptionUtility;
        this.userAccessor = userAccessor;
        this.settingsValidator = settingsValidator;
    }

    @Override
    public FieldModel afterGetAction(FieldModel fieldModel) {
        Optional<UserModel> defaultUser = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        FieldModel fieldModelCopy = createFieldModelCopy(fieldModel);
        String defaultUserEmail = defaultUser.map(UserModel::getEmailAddress).filter(StringUtils::isNotBlank).orElse("");
        boolean defaultUserPasswordSet = defaultUser.map(UserModel::getPassword).filter(StringUtils::isNotBlank).isPresent();
        fieldModelCopy.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL, new FieldValueModel(List.of(defaultUserEmail), StringUtils.isNotBlank(defaultUserEmail)));
        fieldModelCopy.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(null, defaultUserPasswordSet));
        fieldModelCopy.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(null, encryptionUtility.isPasswordSet()));
        fieldModelCopy.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(null, encryptionUtility.isGlobalSaltSet()));
        return fieldModelCopy;
    }

    @Override
    public FieldModel beforeSaveAction(FieldModel fieldModel) {
        return handleNewAndUpdatedConfig(fieldModel);
    }

    @Override
    public FieldModel beforeUpdateAction(FieldModel fieldModel) {
        return handleNewAndUpdatedConfig(fieldModel);
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        handleAfterNewAndUpdate();
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel fieldModel) throws AlertException {
        handleAfterNewAndUpdate();
        return super.afterUpdateAction(fieldModel);
    }

    private FieldModel createFieldModelCopy(FieldModel fieldModel) {
        HashMap<String, FieldValueModel> fields = new HashMap<>();
        fields.putAll(fieldModel.getKeyToValues());

        FieldModel modelToSave = new FieldModel(fieldModel.getDescriptorName(), fieldModel.getContext(),
            fieldModel.getCreatedAt(), fieldModel.getLastUpdated(), fields);
        modelToSave.setId(fieldModel.getId());
        return modelToSave;
    }

    private FieldModel handleNewAndUpdatedConfig(FieldModel fieldModel) {
        saveDefaultAdminUserPassword(fieldModel);
        saveDefaultAdminUserEmail(fieldModel);
        saveEncryptionProperties(fieldModel);
        return scrubModel(fieldModel);
    }

    private void handleAfterNewAndUpdate() {
        settingsValidator.validateUser();
        settingsValidator.validateEncryption();
    }

    private FieldModel scrubModel(FieldModel fieldModel) {
        Map<String, FieldValueModel> keyToValues = new HashMap<>(fieldModel.getKeyToValues());
        keyToValues.remove(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD);
        keyToValues.remove(SettingsDescriptor.KEY_ENCRYPTION_PWD);
        keyToValues.remove(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

        return new FieldModel(fieldModel.getDescriptorName(), fieldModel.getContext(), fieldModel.getCreatedAt(), fieldModel.getLastUpdated(), keyToValues);
    }

    private void saveDefaultAdminUserPassword(FieldModel fieldModel) {
        String password = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).flatMap(FieldValueModel::getValue).orElse("");
        if (StringUtils.isNotBlank(password)) {
            Optional<UserModel> adminUser = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
            if (adminUser.isPresent()) {
                userAccessor.changeUserPassword(adminUser.get().getName(), password);
            } else {
                logger.error("Critical Error. Default admin user could not be found in the database.");
            }
        }
    }

    private void saveDefaultAdminUserEmail(FieldModel fieldModel) {
        Optional<FieldValueModel> optionalEmail = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL);
        if (optionalEmail.isPresent()) {
            Optional<UserModel> adminUser = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
            if (adminUser.isPresent()) {
                userAccessor.changeUserEmailAddress(adminUser.get().getName(), optionalEmail.flatMap(FieldValueModel::getValue).orElse(""));
            } else {
                logger.error("Critical Error. Default admin user could not be found in the database.");
            }
        }
    }

    private void saveEncryptionProperties(FieldModel fieldModel) {
        try {
            Optional<FieldValueModel> optionalEncryptionPassword = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD);
            Optional<FieldValueModel> optionalEncryptionSalt = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

            if (optionalEncryptionPassword.isPresent()) {
                String passwordToSave = optionalEncryptionPassword.get().getValue().orElse("");
                if (StringUtils.isNotBlank(passwordToSave)) {
                    encryptionUtility.updatePasswordField(passwordToSave);
                }
            }

            if (optionalEncryptionSalt.isPresent()) {
                String saltToSave = optionalEncryptionSalt.get().getValue().orElse("");
                if (StringUtils.isNotBlank(saltToSave)) {
                    encryptionUtility.updateSaltField(saltToSave);
                }
            }
        } catch (IllegalArgumentException | IOException ex) {
            logger.error("Error saving encryption configuration.", ex);
        }
    }
}
