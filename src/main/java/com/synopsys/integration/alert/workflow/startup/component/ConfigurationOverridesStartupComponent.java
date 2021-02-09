/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.workflow.startup.component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.DefaultDescriptorGlobalConfigUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.authentication.actions.AuthenticationApiAction;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;

@Component
@Order(11)
public class ConfigurationOverridesStartupComponent extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationOverridesStartupComponent.class);
    private static final String ENV_VAR_LDAP_DISABLE = "ALERT_LDAP_DISABLED";
    private static final String ENV_VAR_SAML_DISABLE = "ALERT_SAML_DISABLED";
    private static final String ENV_VAR_ADMIN_USER_PASSWORD_RESET = "ALERT_ADMIN_USER_PASSWORD_RESET";
    private static final String DEFAULT_ADMIN_USERNAME = "sysadmin";
    private static final String DEFAULT_ADMIN_PASSWORD = "$2a$16$Q3wfnhwA.1Qm3Tz3IkqDC.743C5KI7nJIuYlZ4xKXre/WBYpjUEFy";

    private EnvironmentVariableUtility environmentVariableUtility;
    private final DefaultDescriptorGlobalConfigUtility configUtility;
    private UserAccessor userAccessor;

    @Autowired
    public ConfigurationOverridesStartupComponent(EnvironmentVariableUtility environmentVariableUtility, UserAccessor userAccessor, AuthenticationDescriptorKey descriptorKey, ConfigurationAccessor configurationAccessor,
        AuthenticationApiAction apiAction, ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.environmentVariableUtility = environmentVariableUtility;
        this.configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationAccessor, apiAction, configurationFieldModelConverter);
        this.userAccessor = userAccessor;
    }

    @Override
    protected void initialize() {
        try {
            FieldModel fieldModel = getFieldModel();
            checkAndDisableLdapAuthentication(fieldModel);
            checkAndDisableSamlAuthentication(fieldModel);
            checkAndResetDefaultAdminPassword();
            if (StringUtils.isBlank(fieldModel.getId())) {
                configUtility.save(fieldModel);
            } else {
                configUtility.update(Long.valueOf(fieldModel.getId()), fieldModel);
            }
        } catch (AlertException | NumberFormatException ex) {
            logger.error("Error performing configuration overrides.", ex);
        }
    }

    private FieldModel getFieldModel() throws AlertException {
        Optional<FieldModel> settingsFieldModel = configUtility.getFieldModel();
        return settingsFieldModel
                   .orElse(new FieldModel(configUtility.getKey().getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>()));
    }

    private void checkAndDisableLdapAuthentication(FieldModel fieldModel) {
        checkAndDisableBooleanField(fieldModel, ENV_VAR_LDAP_DISABLE, AuthenticationDescriptor.KEY_LDAP_ENABLED);
    }

    private void checkAndDisableSamlAuthentication(FieldModel fieldModel) {
        checkAndDisableBooleanField(fieldModel, ENV_VAR_SAML_DISABLE, AuthenticationDescriptor.KEY_SAML_ENABLED);
    }

    private void checkAndDisableBooleanField(FieldModel fieldModel, String environmentVariable, String fieldKey) {
        boolean disable = isEnvironmentVariableActivated(environmentVariable);
        if (disable) {
            logger.info("Disabling field: {}", fieldKey);
            FieldValueModel fieldValue = new FieldValueModel(List.of(String.valueOf(Boolean.FALSE)), false);
            fieldModel.putField(fieldKey, fieldValue);
        }
    }

    private void checkAndResetDefaultAdminPassword() throws AlertException {
        boolean disable = isEnvironmentVariableActivated(ENV_VAR_ADMIN_USER_PASSWORD_RESET);
        if (disable) {
            logger.info("Resetting default admin password.");
            UserModel userModel = userAccessor.getUser(DEFAULT_ADMIN_USERNAME)
                                      .orElseThrow(() -> new AlertException("Default Sysadmin user not found."));
            UserModel newModel = UserModel.existingUser(userModel.getId(), userModel.getName(), DEFAULT_ADMIN_PASSWORD, userModel.getEmailAddress(), userModel.getAuthenticationType(), userModel.getRoles(), userModel.isEnabled());
            userAccessor.updateUser(newModel, true);
        }
    }

    private boolean isEnvironmentVariableActivated(String environmentVariable) {
        boolean activated = environmentVariableUtility.getEnvironmentValue(environmentVariable)
                                .map(Boolean::valueOf)
                                .orElse(false);
        logger.info("{} = {}", environmentVariable, activated);
        return activated;
    }
}
