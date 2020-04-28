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
package com.synopsys.integration.alert.workflow.startup.component;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;

@Component
@Order(55)
public class LastSearchDataMigration extends StartupComponent {
    private static final Logger logger = LoggerFactory.getLogger(LastSearchDataMigration.class);
    private static final String LAST_SEARCH_FILE = "blackduck-accumulator-task-last-search.txt";

    // Because pg_stat_file requires admin privileges to run, we have to migrate the last search file data in code.
    // We don't have admin privileges to postgres when alert starts.

    private BlackDuckProviderKey providerKey;
    private ConfigurationAccessor configurationAccessor;
    private FilePersistenceUtil filePersistenceUtil;
    private ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor;

    public LastSearchDataMigration(BlackDuckProviderKey providerKey, ConfigurationAccessor configurationAccessor, FilePersistenceUtil filePersistenceUtil, ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor) {
        this.providerKey = providerKey;
        this.configurationAccessor = configurationAccessor;
        this.filePersistenceUtil = filePersistenceUtil;
        this.providerTaskPropertiesAccessor = providerTaskPropertiesAccessor;
    }

    @Override
    protected void initialize() {
        logger.info("Checking if last search text file data should be migrated.");
        if(filePersistenceUtil.exists(LAST_SEARCH_FILE)) {
            logger.info("Last search text file exists; attempt migration to task properties.");
            try {
                Optional<ConfigurationModel> configuration = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(providerKey, ConfigContextEnum.GLOBAL).stream()
                    .findFirst();
                if(configuration.isPresent()) {
                    logger.info("Configuration found. Creating property data.");
                    Long configId = configuration.get().getConfigurationId();
                    String taskName = String.format("Task::Class[com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator]::Provider[provider_blackduck]::Configuration[id:%s]", configId);
                    String propertyValue = filePersistenceUtil.readFromFile(LAST_SEARCH_FILE);
                    providerTaskPropertiesAccessor.setTaskProperty(configId,taskName, BlackDuckAccumulator.TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE,propertyValue);
                    filePersistenceUtil.delete(LAST_SEARCH_FILE);
                }
            } catch (IOException ex) {
                logger.error("Error with last search text file.", ex);
            } catch(AlertDatabaseConstraintException ex) {
                logger.error("Error writing provider property for default provider configuration.", ex);
            }
        } else {
            logger.info("Last search text file does not exist; no migration necessary.");
        }
    }
}
