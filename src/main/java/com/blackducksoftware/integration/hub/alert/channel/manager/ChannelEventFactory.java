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

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Component
public class ChannelEventFactory<E extends AbstractChannelEvent, D extends DistributionChannelConfigEntity, G extends GlobalChannelConfigEntity, R extends CommonDistributionConfigRestModel> {
    private final List<DistributionChannelManager<G, D, E, R>> channelManagers;

    @Autowired
    public ChannelEventFactory(final List<DistributionChannelManager<G, D, E, R>> channelManagers) {
        this.channelManagers = channelManagers;
    }

    public AbstractChannelEvent createEvent(final Long commonDistributionConfigId, final String distributionType, final Collection<ProjectData> projectDataCollection) {
        for (final DistributionChannelManager<G, D, E, R> manager : channelManagers) {
            if (manager.isApplicable(distributionType)) {
                return manager.createChannelEvent(projectDataCollection, commonDistributionConfigId);
            }
        }
        return null;
    }

}
