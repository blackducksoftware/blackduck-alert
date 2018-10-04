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
package com.synopsys.integration.alert.database.channel.email;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.descriptor.EmailDistributionTypeConverter;
import com.synopsys.integration.alert.database.ChannelDistributionRespositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

@Component
public class EmailDistributionRepositoryAccessor extends ChannelDistributionRespositoryAccessor {
    private final EmailGroupDistributionRepository repository;
    private final EmailDistributionTypeConverter emailDistributionTypeConverter;

    @Autowired
    public EmailDistributionRepositoryAccessor(final EmailGroupDistributionRepository repository, final EmailDistributionTypeConverter emailDistributionTypeConverter) {
        super(repository);
        this.repository = repository;
        this.emailDistributionTypeConverter = emailDistributionTypeConverter;
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final EmailGroupDistributionConfigEntity emailEntity = (EmailGroupDistributionConfigEntity) entity;
        return repository.save(emailEntity);
    }

    @Override
    public Optional<? extends CommonDistributionConfig> getJobConfig(final Long distributionConfigId) {
        Optional<? extends CommonDistributionConfig> optionalConfig = Optional.empty();
        final Optional<? extends DatabaseEntity> optionalDatabaseEntity = readEntity(distributionConfigId);
        if (optionalDatabaseEntity.isPresent()) {
            final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = (EmailGroupDistributionConfigEntity) optionalDatabaseEntity.get();
            final EmailDistributionConfig emailDistributionConfig = (EmailDistributionConfig) emailDistributionTypeConverter.populateConfigFromEntity(emailGroupDistributionConfigEntity);
            optionalConfig = Optional.of(emailDistributionConfig);
        }
        return optionalConfig;
    }
}
