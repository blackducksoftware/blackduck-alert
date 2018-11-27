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
package com.synopsys.integration.alert.database;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synopsys.integration.alert.common.descriptor.config.TypeConverter;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

public abstract class ChannelDistributionRepositoryAccessor extends RepositoryAccessor {
    private final TypeConverter typeConverter;

    public ChannelDistributionRepositoryAccessor(final JpaRepository<? extends DatabaseEntity, Long> repository, final TypeConverter typeConverter) {
        super(repository);
        this.typeConverter = typeConverter;
    }

    public Optional<? extends CommonDistributionConfig> getJobConfig(final Long distributionConfigId) {
        final Optional<? extends DatabaseEntity> optionalDatabaseEntity = readEntity(distributionConfigId);
        if (optionalDatabaseEntity.isPresent()) {
            final DatabaseEntity entity = optionalDatabaseEntity.get();
            final Config distributionConfig = typeConverter.populateConfigFromEntity(entity);
            return Optional.of((CommonDistributionConfig) distributionConfig);
        }
        return Optional.empty();
    }

}
