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
package com.synopsys.integration.alert.web.security.authentication.saml;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertLDAPConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

public class SAMLContext {
    private static final Logger logger = LoggerFactory.getLogger(SAMLContext.class);
    private final ConfigurationAccessor configurationAccessor;

    public SAMLContext(final ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    public ConfigurationModel getCurrentConfiguration() throws AlertDatabaseConstraintException, AlertLDAPConfigurationException {
        return configurationAccessor.getConfigurationsByDescriptorName(SettingsDescriptor.SETTINGS_COMPONENT)
                   .stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertLDAPConfigurationException("Settings configuration missing"));
    }

    public boolean isSAMLEnabled() {
        final boolean enabled = false;
        try {
            return isSAMLEnabled(getCurrentConfiguration());
        } catch (final AlertDatabaseConstraintException | AlertLDAPConfigurationException ex) {
            logger.warn(ex.getMessage());
            logger.debug("cause: ", ex);
        }

        return enabled;
    }

    public boolean isSAMLEnabled(final ConfigurationModel configurationModel) {
        return getFieldValueBoolean(configurationModel, SettingsDescriptor.KEY_SAML_ENABLED);
    }

    public String getFieldValueOrEmpty(final ConfigurationModel configurationModel, final String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");
    }

    public Boolean getFieldValueBoolean(final ConfigurationModel configurationModel, final String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue).map(BooleanUtils::toBoolean).orElse(false);
    }
}
