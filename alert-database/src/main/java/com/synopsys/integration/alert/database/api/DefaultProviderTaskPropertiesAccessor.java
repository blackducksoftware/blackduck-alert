/**
 * alert-database
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
package com.synopsys.integration.alert.database.api;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesEntity;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesRepository;

@Component
@Transactional
public class DefaultProviderTaskPropertiesAccessor implements ProviderTaskPropertiesAccessor {
    private ProviderTaskPropertiesRepository providerTaskPropertiesRepository;

    @Autowired
    public DefaultProviderTaskPropertiesAccessor(ProviderTaskPropertiesRepository providerTaskPropertiesRepository) {
        this.providerTaskPropertiesRepository = providerTaskPropertiesRepository;
    }

    @Override
    public Optional<String> getTaskProperty(String taskName, String propertyKey) {
        if (StringUtils.isBlank(taskName) || StringUtils.isBlank(propertyKey)) {
            return Optional.empty();
        }
        return providerTaskPropertiesRepository.findByTaskNameAndPropertyName(taskName, propertyKey).map(ProviderTaskPropertiesEntity::getValue);
    }

    @Override
    public void setTaskProperty(Long configId, String taskName, String propertyKey, String propertyValue) throws AlertDatabaseConstraintException {
        if (null == configId || StringUtils.isBlank(taskName) || StringUtils.isBlank(propertyKey) || StringUtils.isBlank(propertyValue)) {
            throw new AlertDatabaseConstraintException("All fields are required to save a task property");
        }
        ProviderTaskPropertiesEntity taskPropertyToSave = new ProviderTaskPropertiesEntity(configId, taskName, propertyKey, propertyValue);
        providerTaskPropertiesRepository.save(taskPropertyToSave);
    }

}
