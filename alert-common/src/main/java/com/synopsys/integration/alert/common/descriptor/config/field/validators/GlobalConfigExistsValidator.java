/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.config.field.validators;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class GlobalConfigExistsValidator implements ConfigValidationFunction {
    public static final String GLOBAL_CONFIG_MISSING = "Configuration missing.";
    private static final Logger logger = LoggerFactory.getLogger(GlobalConfigExistsValidator.class);
    private ConfigurationAccessor configurationAccessor;

    @Autowired
    public GlobalConfigExistsValidator(ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public Collection<String> apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        String descriptorName = fieldValueModel.getValue().orElse("");
        try {
            List<ConfigurationModel> configurations = configurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, ConfigContextEnum.GLOBAL);
            if (configurations.isEmpty()) {
                return List.of(GLOBAL_CONFIG_MISSING);
            }
        } catch (AlertDatabaseConstraintException ex) {
            logger.error("Error validating configuration.", ex);
            return List.of(GLOBAL_CONFIG_MISSING);
        }
        return List.of();
    }
}
