/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.CommonDistributionConfigRestModel;

public abstract class DistributionConfigActions<D extends DatabaseEntity, R extends CommonDistributionConfigRestModel> extends ConfigActions<D, R> {
    public final JpaRepository<CommonDistributionConfigEntity, Long> commonDistributionRepository;
    public final JpaRepository<D, Long> repository;
    public final ObjectTransformer objectTransformer;

    public DistributionConfigActions(final Class<D> databaseEntityClass, final Class<R> configRestModelClass, final JpaRepository<CommonDistributionConfigEntity, Long> commonDistributionRepository, final JpaRepository<D, Long> repository,
            final ObjectTransformer objectTransformer) {
        super(databaseEntityClass, configRestModelClass, repository, objectTransformer);
        this.commonDistributionRepository = commonDistributionRepository;
        this.repository = repository;
        this.objectTransformer = objectTransformer;
    }

    @Override
    public List<R> getConfig(final Long id) throws AlertException {
        if (id != null) {
            final D foundEntity = repository.findOne(id);
            if (foundEntity != null) {
                return Arrays.asList(constructRestModel(foundEntity));
            }
            return Collections.emptyList();
        }
        return constructRestModels();
    }

    @Override
    public D saveConfig(final R restModel) throws AlertException {
        if (restModel != null) {
            try {
                D createdEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, databaseEntityClass);
                final CommonDistributionConfigEntity commonEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, CommonDistributionConfigEntity.class);
                if (createdEntity != null && commonEntity != null) {
                    createdEntity = repository.save(createdEntity);
                    commonEntity.setDistributionConfigId(createdEntity.getId());
                    commonDistributionRepository.save(commonEntity);
                    return createdEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public D saveNewConfigUpdateFromSavedConfig(final R restModel) throws AlertException {
        return saveConfig(restModel);
    }

    @Override
    public String validateConfig(final R restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getId()) && !StringUtils.isNumeric(restModel.getId())) {
            fieldErrors.put("id", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getDistributionConfigId()) && !StringUtils.isNumeric(restModel.getDistributionConfigId())) {
            fieldErrors.put("distributionConfigId", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getFilterByProject()) && !isBoolean(restModel.getFilterByProject())) {
            fieldErrors.put("filterByProject", "Not a Boolean.");
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public List<String> sensitiveFields() {
        return Collections.emptyList();
    }

    public List<R> constructRestModels() {
        final List<D> allEntities = repository.findAll();
        final List<R> constructedRestModels = new ArrayList<>(allEntities.size());
        for (final D entity : allEntities) {
            constructedRestModels.add(constructRestModel(entity));
        }
        return constructedRestModels;
    }

    public abstract R constructRestModel(final D entity);

}
