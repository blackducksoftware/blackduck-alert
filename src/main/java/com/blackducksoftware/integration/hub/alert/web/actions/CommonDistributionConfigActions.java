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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.CommonDistributionConfigRestModel;

@Component
public class CommonDistributionConfigActions extends DistributionConfigActions<CommonDistributionConfigEntity, CommonDistributionConfigRestModel> {
    private final Logger logger = LoggerFactory.getLogger(CommonDistributionConfigActions.class);

    public CommonDistributionConfigActions(final JpaRepository<CommonDistributionConfigEntity, Long> repository, final ObjectTransformer objectTransformer) {
        super(CommonDistributionConfigEntity.class, CommonDistributionConfigRestModel.class, repository, repository, objectTransformer);
    }

    @Override
    public CommonDistributionConfigEntity saveConfig(final CommonDistributionConfigRestModel restModel) throws AlertException {
        if (restModel != null) {
            try {
                CommonDistributionConfigEntity createdEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, databaseEntityClass);
                if (createdEntity != null) {
                    createdEntity = commonDistributionRepository.save(createdEntity);
                    return createdEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public CommonDistributionConfigRestModel constructRestModel(final CommonDistributionConfigEntity entity) {
        CommonDistributionConfigRestModel restModel = null;
        if (entity != null) {
            try {
                restModel = objectTransformer.databaseEntityToConfigRestModel(entity, CommonDistributionConfigRestModel.class);
            } catch (final AlertException e) {
                logger.warn("Problem constructing rest model", e);
            }
        }
        return restModel;
    }

    @Override
    public String channelTestConfig(final CommonDistributionConfigRestModel restModel) throws IntegrationException {
        // TODO test config
        return null;
    }

}
