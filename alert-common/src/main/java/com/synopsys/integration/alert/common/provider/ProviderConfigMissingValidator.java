/**
 * alert-common
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
package com.synopsys.integration.alert.common.provider;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.lifecycle.ProvidersMissingTask;
import com.synopsys.integration.alert.common.system.BaseSystemValidator;

@Component
public class ProviderConfigMissingValidator extends BaseSystemValidator {
    public static final String MISSING_BLACKDUCK_CONFIG_ERROR_FORMAT = "Black Duck configuration is invalid. Black Duck configurations missing.";
    private Logger logger = LoggerFactory.getLogger(ProvidersMissingTask.class);
    private List<Provider> providers;
    private ConfigurationAccessor configurationAccessor;

    @Autowired
    public ProviderConfigMissingValidator(SystemMessageAccessor systemMessageAccessor, List<Provider> providers,
        ConfigurationAccessor configurationAccessor) {
        super(systemMessageAccessor);
        this.providers = providers;
        this.configurationAccessor = configurationAccessor;
    }

    public void validate() {
        removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_CONFIGURATION_MISSING);
        for (Provider provider : providers) {
            try {
                List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(provider.getKey(), ConfigContextEnum.GLOBAL);
                boolean emptyConfiguration = isConfigurationsEmpty(configurations);
                if (emptyConfiguration) {
                    addSystemMessageForError(MISSING_BLACKDUCK_CONFIG_ERROR_FORMAT, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONFIGURATION_MISSING, true);
                }
            } catch (AlertDatabaseConstraintException ex) {
                logger.debug("Error getting provider configurations", ex);
                addSystemMessageForError(MISSING_BLACKDUCK_CONFIG_ERROR_FORMAT, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONFIGURATION_MISSING, true);
            }
        }
    }

    private boolean isConfigurationsEmpty(List<ConfigurationModel> configurations) {
        if (configurations.isEmpty()) {
            return true;
        }
        boolean emptyModels = configurations.stream()
                                  .map(ConfigurationModel::getCopyOfFieldList)
                                  .allMatch(List::isEmpty);
        return emptyModels;
    }
}
