/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.UserModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.workflow.event.ConfigurationEvent;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;

@Component
public class SettingsEventListener {
    private static final Logger logger = LoggerFactory.getLogger(SettingsEventListener.class);
    private final EncryptionUtility encryptionUtility;
    private final DefaultUserAccessor userAccessor;
    private final SystemValidator systemValidator;
    // TODO enable SAML support
    // private final SAMLManager samlManager;

    @Autowired
    public SettingsEventListener(final EncryptionUtility encryptionUtility, final DefaultUserAccessor userAccessor, final SystemValidator systemValidator) {
        this.encryptionUtility = encryptionUtility;
        this.userAccessor = userAccessor;
        this.systemValidator = systemValidator;
        // TODO enable SAML support
        // this.samlManager = samlManager;
    }

    @EventListener(condition = "#configurationEvent.configurationName == 'component_settings' && #configurationEvent.eventType.name() == 'CONFIG_GET_AFTER'")
    public void handleReadConfig(final ConfigurationEvent configurationEvent) {
        final FieldModel fieldModel = configurationEvent.getFieldModel();
        final Optional<UserModel> defaultUser = userAccessor.getUser(DefaultUserAccessor.DEFAULT_ADMIN_USER);
        final boolean defaultUserPasswordSet = defaultUser.map(UserModel::getPassword).filter(StringUtils::isNotBlank).isPresent();
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(null, defaultUserPasswordSet));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(null, encryptionUtility.isPasswordSet()));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(null, encryptionUtility.isGlobalSaltSet()));
    }

    @EventListener(condition = "#configurationEvent.configurationName == 'component_settings' && (#configurationEvent.eventType.name() == 'CONFIG_UPDATE_BEFORE' || #configurationEvent.eventType.name() == 'CONFIG_SAVE_BEFORE')")
    public void handleNewAndUpdatedConfig(final ConfigurationEvent configurationEvent) {
        final FieldModel fieldModel = configurationEvent.getFieldModel();
        saveDefaultAdminUserPassword(fieldModel);
        saveDefaultAdminUserEmail(fieldModel);
        saveEncryptionProperties(fieldModel);
        addSAMLMetadata(fieldModel);
        systemValidator.validate();
        scrubModel(fieldModel);
    }

    private void scrubModel(final FieldModel fieldModel) {
        fieldModel.removeField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD);
        fieldModel.removeField(SettingsDescriptor.KEY_ENCRYPTION_PWD);
        fieldModel.removeField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);
    }

    private void saveDefaultAdminUserPassword(final FieldModel fieldModel) {
        final String password = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).flatMap(FieldValueModel::getValue).orElse("");
        if (StringUtils.isNotBlank(password)) {
            userAccessor.changeUserPassword(DefaultUserAccessor.DEFAULT_ADMIN_USER, password);
        }
    }

    private void saveDefaultAdminUserEmail(final FieldModel fieldModel) {
        final Optional<FieldValueModel> optionalEmail = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL);
        if (optionalEmail.isPresent()) {
            userAccessor.changeUserEmailAddress(DefaultUserAccessor.DEFAULT_ADMIN_USER, optionalEmail.flatMap(FieldValueModel::getValue).orElse(""));
        }
    }

    private void saveEncryptionProperties(final FieldModel fieldModel) {
        try {
            final Optional<FieldValueModel> optionalEncryptionPassword = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD);
            final Optional<FieldValueModel> optionalEncryptionSalt = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

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

    private void addSAMLMetadata(final FieldModel fieldModel) {
        // TODO enable SAML support
        /*final Boolean samlEnabled = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_SAML_ENABLED)
                                        .map(fieldValueModel -> fieldValueModel.getValue()
                                                                    .map(BooleanUtils::toBoolean)
                                                                    .orElse(false)
                                        ).orElse(false);
        final Optional<FieldValueModel> metadataURLFieldValueOptional = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_SAML_METADATA_URL);
        if (metadataURLFieldValueOptional.isPresent()) {
            final FieldValueModel metadataURLFieldValue = metadataURLFieldValueOptional.get();
            final String metadataURL = metadataURLFieldValue.getValue().orElse("");
            samlManager.updateSAMLConfiguration(samlEnabled, metadataURL);
        }*/
    }
}
