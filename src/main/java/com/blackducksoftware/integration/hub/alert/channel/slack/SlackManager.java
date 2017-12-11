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
package com.blackducksoftware.integration.hub.alert.channel.slack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.manager.DistributionChannelManager;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;

@Component
public class SlackManager extends DistributionChannelManager<GlobalSlackConfigEntity, SlackDistributionConfigEntity, SlackEvent> {
    public SlackManager(final DistributionChannel<SlackEvent, GlobalSlackConfigEntity, SlackDistributionConfigEntity> distributionChannel, final JpaRepository<GlobalSlackConfigEntity, Long> globalRepository,
            final JpaRepository<SlackDistributionConfigEntity, Long> localRepository) {
        super(distributionChannel, globalRepository, localRepository);
    }

    @Override
    public SlackEvent createChannelEvent(final ProjectData projectData, final Long commonDistributionConfigId) {
        return new SlackEvent(projectData, commonDistributionConfigId);
    }

}
