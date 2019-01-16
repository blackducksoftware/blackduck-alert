/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;

public abstract class DataUpgrade {
    private final String descriptorName;
    private final JpaRepository<? extends DatabaseEntity, Long> repository;
    private final ConfigContextEnum context;
    private final BaseConfigurationAccessor configurationAccessor;

    public DataUpgrade(final String descriptorName, final JpaRepository<? extends DatabaseEntity, Long> repository, final ConfigContextEnum context, final BaseConfigurationAccessor configurationAccessor) {
        this.descriptorName = descriptorName;
        this.repository = repository;
        this.context = context;
        this.configurationAccessor = configurationAccessor;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public void upgrade() throws AlertDatabaseConstraintException {
        final List<? extends DatabaseEntity> entities = repository.findAll();
        for (final DatabaseEntity entity : entities) {
            final List<ConfigurationFieldModel> fieldModels = convertEntityToFieldList(entity);
            configurationAccessor.createConfiguration(getDescriptorName(), context, fieldModels);
        }
    }

    public abstract List<ConfigurationFieldModel> convertEntityToFieldList(DatabaseEntity databaseEntity);

}
