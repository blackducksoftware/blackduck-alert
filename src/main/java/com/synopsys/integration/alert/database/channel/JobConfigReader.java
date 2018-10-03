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

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDistributionTypeConverter;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDistributionTypeConverter;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDistributionTypeConverter;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

@Component
public class JobConfigReader {
    private final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor;
    private final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor;
    private final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor;
    private final CommonDistributionRepository commonDistributionRepository;
    private final EmailDistributionTypeConverter emailDistributionTypeConverter;
    private final HipChatDistributionTypeConverter hipChatDistributionTypeConverter;
    private final SlackDistributionTypeConverter slackDistributionTypeConverter;

    @Autowired
    public JobConfigReader(final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor,
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor, final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor,
        final CommonDistributionRepository commonDistributionRepository, final EmailDistributionTypeConverter emailDistributionTypeConverter,
        final HipChatDistributionTypeConverter hipChatDistributionTypeConverter, final SlackDistributionTypeConverter slackDistributionTypeConverter) {
        this.emailDistributionRepositoryAccessor = emailDistributionRepositoryAccessor;
        this.hipChatDistributionRepositoryAccessor = hipChatDistributionRepositoryAccessor;
        this.slackDistributionRepositoryAccessor = slackDistributionRepositoryAccessor;
        this.commonDistributionRepository = commonDistributionRepository;
        this.emailDistributionTypeConverter = emailDistributionTypeConverter;
        this.hipChatDistributionTypeConverter = hipChatDistributionTypeConverter;
        this.slackDistributionTypeConverter = slackDistributionTypeConverter;
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
        final Optional<CommonDistributionConfigEntity> foundEntity = commonDistributionRepository.findById(configId);
        if (foundEntity.isPresent()) {
            final CommonDistributionConfigEntity configEntity = foundEntity.get();
            return getJobConfig(configEntity.getDistributionConfigId(), configEntity.getDistributionType());
        } else {
            return Optional.empty();
        }
    }

    private Optional<? extends CommonDistributionConfig> getJobConfig(final Long distributionConfigId, final String distributionType) {
        Optional<? extends CommonDistributionConfig> optionalConfig = Optional.empty();
        if (distributionType.equals(EmailGroupChannel.COMPONENT_NAME)) {
            final Optional<? extends DatabaseEntity> optionalDatabaseEntity = emailDistributionRepositoryAccessor.readEntity(distributionConfigId);
            if (optionalDatabaseEntity.isPresent()) {
                final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = (EmailGroupDistributionConfigEntity) optionalDatabaseEntity.get();
                final EmailDistributionConfig emailDistributionConfig = (EmailDistributionConfig) emailDistributionTypeConverter.populateConfigFromEntity(emailGroupDistributionConfigEntity);
                optionalConfig = Optional.of(emailDistributionConfig);
            }
        } else if (distributionType.equals(HipChatChannel.COMPONENT_NAME)) {
            final Optional<? extends DatabaseEntity> optionalDatabaseEntity = hipChatDistributionRepositoryAccessor.readEntity(distributionConfigId);
            if (optionalDatabaseEntity.isPresent()) {
                final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = (HipChatDistributionConfigEntity) optionalDatabaseEntity.get();
                final HipChatDistributionConfig hipChatDistributionConfig = (HipChatDistributionConfig) hipChatDistributionTypeConverter.populateConfigFromEntity(hipChatDistributionConfigEntity);
                optionalConfig = Optional.of(hipChatDistributionConfig);
            }
        } else if (distributionType.equals(SlackChannel.COMPONENT_NAME)) {
            final Optional<? extends DatabaseEntity> optionalDatabaseEntity = slackDistributionRepositoryAccessor.readEntity(distributionConfigId);
            if (optionalDatabaseEntity.isPresent()) {
                final SlackDistributionConfigEntity slackDistributionConfigEntity = (SlackDistributionConfigEntity) optionalDatabaseEntity.get();
                final SlackDistributionConfig slackDistributionConfig = (SlackDistributionConfig) slackDistributionTypeConverter.populateConfigFromEntity(slackDistributionConfigEntity);
                optionalConfig = Optional.of(slackDistributionConfig);
            }
        }
        return optionalConfig;
    }

}
