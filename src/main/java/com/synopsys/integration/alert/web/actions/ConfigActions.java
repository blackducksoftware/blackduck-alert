/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

public abstract class ConfigActions {
    private final ContentConverter contentConverter;
    private final DefaultConfigActions defaultConfigActions;

    public ConfigActions(final ContentConverter contentConverter, final DefaultConfigActions defaultConfigActions) {
        this.contentConverter = contentConverter;
        this.defaultConfigActions = defaultConfigActions;
    }

    public boolean doesConfigExist(final String id, final DescriptorConfig descriptorConfig) {
        return doesConfigExist(contentConverter.getLongValue(id), descriptorConfig);
    }

    public boolean doesConfigExist(final Long id, final DescriptorConfig descriptorConfig) {
        return defaultConfigActions.doesConfigExist(id, descriptorConfig.getRepositoryAccessor());
    }

    public List<? extends Config> getConfig(final Long id, final DescriptorConfig descriptorConfig) throws AlertException {
        if (id != null) {
            final Config config = defaultConfigActions.getConfig(id, descriptorConfig.getRepositoryAccessor(), descriptorConfig.getTypeConverter());
            if (config != null) {
                return Arrays.asList(config);
            }
            return Collections.emptyList();
        }
        return defaultConfigActions.getConfigs(descriptorConfig.getRepositoryAccessor(), descriptorConfig.getTypeConverter());
    }

    public void deleteConfig(final String id, final DescriptorConfig descriptorConfig) {
        deleteConfig(contentConverter.getLongValue(id), descriptorConfig);
    }

    public void deleteConfig(final Long id, final DescriptorConfig descriptorConfig) {
        defaultConfigActions.deleteConfig(id, descriptorConfig.getRepositoryAccessor());
    }

    public DatabaseEntity saveConfig(final Config config, final DescriptorConfig descriptorConfig) {
        return defaultConfigActions.saveConfig(config, descriptorConfig.getRepositoryAccessor(), descriptorConfig.getTypeConverter());
    }

    public String validateConfig(final Config config, final DescriptorConfig descriptorConfig) throws AlertFieldException {
        final Map<String, String> fieldErrors = Maps.newHashMap();
        descriptorConfig.validateConfig(config, fieldErrors);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public String testConfig(final Config config, final DescriptorConfig descriptorConfig) throws IntegrationException {
        final DatabaseEntity entity = descriptorConfig.populateEntityFromConfig(config);
        descriptorConfig.testConfig(entity);
        return "Succesfully sent test message.";
    }

    public DatabaseEntity updateConfig(final Config config, final DescriptorConfig descriptorConfig) throws AlertException {
        return defaultConfigActions.updateConfig(config, descriptorConfig.getRepositoryAccessor(), descriptorConfig.getTypeConverter());
    }

    public ContentConverter getContentConverter() {
        return contentConverter;
    }

    public DefaultConfigActions getDefaultConfigActions() {
        return defaultConfigActions;
    }

}
