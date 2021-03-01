/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.workflow.startup.component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.task.BlackDuckAccumulator;

@Component
@Order(55)
@Deprecated(since = "6.0.0")
//TODO Remove this class in 8.0.0
/**
 * This class is to move the last search time from a String in a file into the database.
 *
 * This class should be removed in 8.0.0.
 * @deprecated since 6.0.0
 */
public class LastSearchDataMigration extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(LastSearchDataMigration.class);
    private static final String LAST_SEARCH_FILE = "blackduck-accumulator-task-last-search.txt";

    // Because pg_stat_file requires admin privileges to run, we have to migrate the last search file data in code.
    // We don't have admin privileges to postgres when alert starts.

    private final BlackDuckProviderKey blackDuckProviderKey;
    private final ConfigurationAccessor configurationAccessor;
    private final FilePersistenceUtil filePersistenceUtil;
    private final ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor;

    public LastSearchDataMigration(BlackDuckProviderKey providerKey, ConfigurationAccessor configurationAccessor, FilePersistenceUtil filePersistenceUtil, ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor) {
        this.blackDuckProviderKey = providerKey;
        this.configurationAccessor = configurationAccessor;
        this.filePersistenceUtil = filePersistenceUtil;
        this.providerTaskPropertiesAccessor = providerTaskPropertiesAccessor;
    }

    @Override
    protected void initialize() {
        logger.info("Checking if last search text file data should be migrated.");
        if (filePersistenceUtil.exists(LAST_SEARCH_FILE)) {
            logger.info("Last search text file exists; attempt migration to task properties.");
            try {
                List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(blackDuckProviderKey, ConfigContextEnum.GLOBAL);
                if (configurationModels.size() == 1) {
                    logger.info("Configuration found. Creating property data.");
                    Long configId = configurationModels.get(0).getConfigurationId();
                    String taskName = String.format("Task::Class[com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator]::Provider[provider_blackduck]::Configuration[id:%s]", configId);
                    String propertyValue = filePersistenceUtil.readFromFile(LAST_SEARCH_FILE);
                    Optional<String> currentPropertyValue = providerTaskPropertiesAccessor.getTaskProperty(taskName, BlackDuckAccumulator.TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE);
                    if (currentPropertyValue.isEmpty()) {
                        providerTaskPropertiesAccessor.setTaskProperty(configId, taskName, BlackDuckAccumulator.TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE, propertyValue);
                    }
                    filePersistenceUtil.delete(LAST_SEARCH_FILE);
                }
            } catch (IOException ex) {
                logger.error("Error with last search text file.", ex);
            }
        } else {
            logger.info("Last search text file does not exist; no migration necessary.");
        }
    }

}
