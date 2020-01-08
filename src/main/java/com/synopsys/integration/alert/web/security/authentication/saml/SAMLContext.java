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

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;

public class SAMLContext {
    private static final Logger logger = LoggerFactory.getLogger(SAMLContext.class);
    private AuthenticationDescriptorKey descriptorKey;
    private ConfigurationAccessor configurationAccessor;

    public SAMLContext(AuthenticationDescriptorKey descriptorKey, ConfigurationAccessor configurationAccessor) {
        this.descriptorKey = descriptorKey;
        this.configurationAccessor = configurationAccessor;
    }

    public ConfigurationModel getCurrentConfiguration() throws AlertException {
        return configurationAccessor.getConfigurationByDescriptorKeyAndContext(descriptorKey, ConfigContextEnum.GLOBAL).stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertConfigurationException("Settings configuration missing"));
    }

    public boolean isSAMLEnabled() {
        try {
            Optional<ConfigurationModel> samlConfig = configurationAccessor.getConfigurationByDescriptorKeyAndContext(descriptorKey, ConfigContextEnum.GLOBAL)
                                                          .stream()
                                                          .findFirst();
            return isSAMLEnabled(samlConfig);
        } catch (AlertException ex) {
            logger.warn(ex.getMessage());
            logger.debug("cause: ", ex);
        }

        return false;
    }

    public boolean isSAMLEnabled(ConfigurationModel configurationModel) {
        return getFieldValueBoolean(configurationModel, AuthenticationDescriptor.KEY_SAML_ENABLED);
    }

    public String getFieldValueOrEmpty(ConfigurationModel configurationModel, String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");
    }

    public Boolean getFieldValueBoolean(ConfigurationModel configurationModel, String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue).map(BooleanUtils::toBoolean).orElse(false);
    }

    private boolean isSAMLEnabled(Optional<ConfigurationModel> configurationModel) {
        if (configurationModel.isPresent()) {
            return isSAMLEnabled(configurationModel.get());
        }

        return false;
    }

}
