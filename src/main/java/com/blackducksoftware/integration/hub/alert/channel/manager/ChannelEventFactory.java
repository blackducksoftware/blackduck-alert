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
package com.blackducksoftware.integration.hub.alert.channel.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Component
public class ChannelEventFactory<D extends DistributionChannelConfigEntity, G extends GlobalChannelConfigEntity, R extends CommonDistributionConfigRestModel> {
    private final List<DistributionChannelManager<G, D, R>> channelManagers;

    @Autowired
    public ChannelEventFactory(final List<DistributionChannelManager<G, D, R>> channelManagers) {
        this.channelManagers = channelManagers;
    }

    public ChannelEvent createEvent(final Long commonDistributionConfigId, final String distributionType, final DigestModel digestModel) {
        for (final DistributionChannelManager<G, D, R> manager : channelManagers) {
            if (manager.isApplicable(distributionType)) {
                return manager.createChannelEvent(digestModel, commonDistributionConfigId);
            }
        }
        return null;
    }

}
