/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.config.field.validators;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

@Component
public class GlobalConfigExistsValidator {
    public static final String GLOBAL_CONFIG_MISSING = "%s global configuration missing.";
    private static final Logger logger = LoggerFactory.getLogger(GlobalConfigExistsValidator.class);
    private ConfigurationAccessor configurationAccessor;
    private List<DescriptorKey> descriptorKeys;

    @Autowired
    public GlobalConfigExistsValidator(ConfigurationAccessor configurationAccessor, List<DescriptorKey> descriptorKeys) {
        this.configurationAccessor = configurationAccessor;
        this.descriptorKeys = descriptorKeys;
    }

    public Optional<String> validate(String descriptorName) {
        String descriptorDisplayName = descriptorKeys.stream()
                                           .filter(descriptorKey -> descriptorKey.getUniversalKey().equals(descriptorName))
                                           .map(descriptorKey -> descriptorKey.getDisplayName())
                                           .findFirst()
                                           .orElse(descriptorName);
        try {
            List<ConfigurationModel> configurations = configurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, ConfigContextEnum.GLOBAL);
            if (configurations.isEmpty()) {
                return Optional.of(String.format(GLOBAL_CONFIG_MISSING, descriptorDisplayName));
            }
        } catch (AlertDatabaseConstraintException ex) {
            logger.error(String.format("Error validating configuration for %s.", descriptorName), ex);
            return Optional.of(String.format(GLOBAL_CONFIG_MISSING, descriptorDisplayName));
        }
        return Optional.empty();
    }
}
