/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.alert.web.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;

@Component
public class CommonDistributionContentConverter extends DatabaseContentConverter {

    @Autowired
    public CommonDistributionContentConverter(final ContentConverter contentConverter) {
        super(contentConverter);
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        return getContentConverter().getJsonContent(json, CommonDistributionConfigRestModel.class);
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final CommonDistributionConfigRestModel commonRestModel = (CommonDistributionConfigRestModel) restModel;
        final Long distributionConfigId = getContentConverter().getLongValue(commonRestModel.getDistributionConfigId());
        final DigestType digestType = Enum.valueOf(DigestType.class, commonRestModel.getFrequency());
        final Boolean filterByProject = getContentConverter().getBooleanValue(commonRestModel.getFilterByProject());
        final CommonDistributionConfigEntity commonEntity = new CommonDistributionConfigEntity(distributionConfigId, commonRestModel.getDistributionType(), commonRestModel.getName(), digestType, filterByProject);
        addIdToEntityPK(commonRestModel.getId(), commonEntity);
        return commonEntity;
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final CommonDistributionConfigEntity commonEntity = (CommonDistributionConfigEntity) entity;
        final CommonDistributionConfigRestModel commonRestModel = new CommonDistributionConfigRestModel();
        commonRestModel.setId(getContentConverter().getStringValue(commonEntity.getId()));
        commonRestModel.setDistributionConfigId(getContentConverter().getStringValue(entity.getId()));
        commonRestModel.setDistributionType(commonEntity.getDistributionType());
        commonRestModel.setFilterByProject(getContentConverter().getStringValue(commonEntity.getFilterByProject()));
        commonRestModel.setFrequency(commonEntity.getFrequency().name());
        commonRestModel.setName(commonEntity.getName());
        return commonRestModel;
    }

}
