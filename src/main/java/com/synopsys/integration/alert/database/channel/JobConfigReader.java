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
package com.synopsys.integration.alert.database.channel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

@Component
public class JobConfigReader {
    private final CommonDistributionRepository commonDistributionRepository;
    private final DescriptorMap descriptorMap;

    @Autowired
    public JobConfigReader(final CommonDistributionRepository commonDistributionRepository, final DescriptorMap descriptorMap) {
        this.commonDistributionRepository = commonDistributionRepository;
        this.descriptorMap = descriptorMap;
    }

    @Transactional
    public List<? extends CommonDistributionConfig> getPopulatedConfigs() {
        final List<CommonDistributionConfigEntity> foundEntities = commonDistributionRepository.findAll();

        final List<? extends CommonDistributionConfig> configs = foundEntities
                                                                         .stream()
                                                                         .map(entity -> {
                                                                             final Optional<? extends CommonDistributionConfig> optionalCommonDistributionConfig = getJobConfig(entity.getDistributionConfigId(), entity.getDistributionType());
                                                                             if (optionalCommonDistributionConfig.isPresent()) {
                                                                                 return optionalCommonDistributionConfig.get();
                                                                             }
                                                                             return null;
                                                                         })
                                                                         .filter(commonDistributionConfig -> null != commonDistributionConfig)
                                                                         .collect(Collectors.toList());

        return configs;
    }

    @Transactional
    public Optional<? extends CommonDistributionConfig> getPopulatedConfig(final Long configId) {
        if (null == configId) {
            return Optional.empty();
        }
        final Optional<CommonDistributionConfigEntity> foundEntity = commonDistributionRepository.findById(configId);
        if (foundEntity.isPresent()) {
            final CommonDistributionConfigEntity configEntity = foundEntity.get();
            return getJobConfig(configEntity.getDistributionConfigId(), configEntity.getDistributionType());
        } else {
            return Optional.empty();
        }
    }

    private Optional<? extends CommonDistributionConfig> getJobConfig(final Long distributionConfigId, final String distributionType) {
        final Optional<? extends CommonDistributionConfig> optionalConfig = descriptorMap.getChannelDescriptor(distributionType).getChannelDistributionRepositoryAccessor().getJobConfig(distributionConfigId);
        return optionalConfig;
    }

}
