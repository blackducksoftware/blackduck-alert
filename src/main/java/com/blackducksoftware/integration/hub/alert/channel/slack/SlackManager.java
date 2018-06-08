/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel.slack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.manager.DistributionChannelManager;
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

@Component
public class SlackManager extends DistributionChannelManager<GlobalSlackConfigEntity, SlackDistributionConfigEntity, SlackDistributionRestModel> {
    @Autowired
    public SlackManager(final SlackChannel distributionChannel, final GlobalSlackRepository globalRepository, final SlackDistributionRepository localRepository, final ObjectTransformer objectTransformer,
            final AlertEventContentConverter contentConverter) {
        super(distributionChannel, globalRepository, localRepository, objectTransformer, contentConverter);
    }

    @Override
    public boolean isApplicable(final String supportedChannelName) {
        return SlackChannel.COMPONENT_NAME.equals(supportedChannelName);
    }

    @Override
    public ChannelEvent createChannelEvent(final DigestModel content, final Long commonDistributionConfigId) {
        return createChannelEvent(SlackChannel.COMPONENT_NAME, content, commonDistributionConfigId);
    }

    @Override
    public Class<SlackDistributionConfigEntity> getDatabaseEntityClass() {
        return SlackDistributionConfigEntity.class;
    }

}
