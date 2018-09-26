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
package com.synopsys.integration.alert.common.distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.config.CommonTypeConverter;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

@Component
public class CommonDistributionConfigReader {
    private final CommonDistributionRepository commonDistributionRepository;
    private final CommonTypeConverter commonTypeConverter;

    @Autowired
    public CommonDistributionConfigReader(final CommonDistributionRepository commonDistributionRepository, final CommonTypeConverter commonTypeConverter) {
        this.commonDistributionRepository = commonDistributionRepository;
        this.commonTypeConverter = commonTypeConverter;
    }

    @Transactional
    public List<CommonDistributionConfig> getPopulatedConfigs() {
        final List<CommonDistributionConfigEntity> foundEntities = commonDistributionRepository.findAll();

        final List<CommonDistributionConfig> configs = new ArrayList<>(foundEntities.size());
        foundEntities.forEach(entity -> {
            final CommonDistributionConfig newConfig = new CommonDistributionConfig();
            commonTypeConverter.populateCommonFieldsFromEntity(newConfig, entity);
            configs.add(newConfig);
        });
        return configs;
    }

    @Transactional
    public Optional<CommonDistributionConfig> getPopulatedConfig(final Long configId) {
        final Optional<CommonDistributionConfigEntity> foundEntity = commonDistributionRepository.findById(configId);

        if (foundEntity.isPresent()) {
            final CommonDistributionConfigEntity entity = foundEntity.get();
            final CommonDistributionConfig newConfig = new CommonDistributionConfig();
            commonTypeConverter.populateCommonFieldsFromEntity(newConfig, entity);
            return Optional.of(newConfig);
        } else {
            return Optional.empty();
        }
    }
}
