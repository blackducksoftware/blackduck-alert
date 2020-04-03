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
package com.synopsys.integration.alert.common.descriptor.config;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

@Component
public class GlobalConfigExistsValidator {
    public static final String GLOBAL_CONFIG_MISSING = "%s global configuration missing.";
    private static final Logger logger = LoggerFactory.getLogger(GlobalConfigExistsValidator.class);
    private ConfigurationAccessor configurationAccessor;
    private List<Descriptor> descriptors;

    @Autowired
    public GlobalConfigExistsValidator(ConfigurationAccessor configurationAccessor, List<Descriptor> descriptors) {
        this.configurationAccessor = configurationAccessor;
        this.descriptors = descriptors;
    }

    /**
     * @return An Optional<String> containing the error message.
     */
    public Optional<String> validate(String descriptorName) {
        if (StringUtils.isBlank(descriptorName)) {
            return Optional.empty();
        }

        Optional<DescriptorKey> optionalDescriptorKey = descriptors
                                                            .stream()
                                                            .filter(desc -> desc.getDescriptorKey().getUniversalKey().equals(descriptorName))
                                                            .filter(this::hasGlobalConfig)
                                                            .map(Descriptor::getDescriptorKey)
                                                            .findFirst();
        if (optionalDescriptorKey.isEmpty()) {
            return Optional.empty();
        }

        String descriptorDisplayName = optionalDescriptorKey.map(DescriptorKey::getDisplayName).orElse(descriptorName);
        try {
            List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorNameAndContext(descriptorName, ConfigContextEnum.GLOBAL);
            if (configurations.isEmpty()) {
                return Optional.of(String.format(GLOBAL_CONFIG_MISSING, descriptorDisplayName));
            }
        } catch (AlertDatabaseConstraintException ex) {
            logger.error(String.format("Error validating configuration for %s.", descriptorName), ex);
            return Optional.of(String.format(GLOBAL_CONFIG_MISSING, descriptorDisplayName));
        }
        return Optional.empty();
    }

    /**
     * Determines if the descriptor's Global UI Config has fields.
     */
    private boolean hasGlobalConfig(Descriptor descriptor) {
        return descriptor
                   .getUIConfig(ConfigContextEnum.GLOBAL)
                   .map(UIConfig::createFields)
                   .map(List::size)
                   .filter(size -> size > 0)
                   .isPresent();
    }

}
