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
package com.blackducksoftware.integration.alert.startup.migration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.datasource.entity.repository.CommonDistributionRepository;

@Component
public class DistributionJobMigration {
    private final Logger logger = LoggerFactory.getLogger(DistributionJobMigration.class);
    private final CommonDistributionRepository commonDistributionRepository;

    @Autowired
    public DistributionJobMigration(final CommonDistributionRepository commonDistributionRepository) {
        this.commonDistributionRepository = commonDistributionRepository;
    }

    public void jobMigrationMajorVersionOneToTwo() {
        try {
            final List<CommonDistributionConfigEntity> commonDistributionConfigEntities = commonDistributionRepository.findAll();
            logger.info(String.format("Number of jobs: %s", commonDistributionConfigEntities.size()));
            for (final CommonDistributionConfigEntity storedCommonEntity : commonDistributionConfigEntities) {
                try {
                    if (storedCommonEntity.getDistributionType().equals("email_group_channel")) {
                        logger.info(String.format("Updating the Email Job %s from the 1.0.0 format to the 2.0.0 format", storedCommonEntity.getName()));
                        updateCommonEntityDistributionType(storedCommonEntity, EmailGroupChannel.COMPONENT_NAME);
                    } else if (storedCommonEntity.getDistributionType().equals("hipchat_channel")) {
                        logger.info(String.format("Updating the HipChat Job %s from the 1.0.0 format to the 2.0.0 format", storedCommonEntity.getName()));
                        updateCommonEntityDistributionType(storedCommonEntity, HipChatChannel.COMPONENT_NAME);
                    } else if (storedCommonEntity.getDistributionType().equals("slack_channel")) {
                        logger.info(String.format("Updating the Slack Job %s from the 1.0.0 format to the 2.0.0 format", storedCommonEntity.getName()));
                        updateCommonEntityDistributionType(storedCommonEntity, SlackChannel.COMPONENT_NAME);
                    }
                } catch (final Exception e) {
                    logger.error(String.format("Could not migrate the Job %s from the 1.0.0 format to the 2.0.0 format: %s", storedCommonEntity.getName(), e.getMessage()), e);
                }
            }
        } catch (final Exception e) {
            logger.error(String.format("Could not migrate the Jobs from the 1.0.0 format to the 2.0.0 format: %s", e.getMessage()), e);
        }
    }

    private void updateCommonEntityDistributionType(final CommonDistributionConfigEntity storedCommonEntity, final String distributionType) {
        logger.info(String.format("Previous distribution id: %s, id: %s, Type: %s", storedCommonEntity.getDistributionConfigId(), storedCommonEntity.getId(), storedCommonEntity.getDistributionType()));
        storedCommonEntity.setDistributionType(distributionType);
        final CommonDistributionConfigEntity savedCommonEntity = commonDistributionRepository.save(storedCommonEntity);
        logger.info(String.format("Saved distribution id: %s, id: %s, Type: %s", savedCommonEntity.getDistributionConfigId(), savedCommonEntity.getId(), savedCommonEntity.getDistributionType()));
    }
}
