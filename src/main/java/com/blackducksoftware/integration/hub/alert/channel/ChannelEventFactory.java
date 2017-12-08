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
package com.blackducksoftware.integration.hub.alert.channel;

import java.util.List;

import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

@Component
public class ChannelEventFactory<E extends AbstractChannelEvent, D extends DatabaseEntity, G extends DatabaseEntity> {
    private final List<DistributionChannel<E, D, G>> distributionChannels;

    public ChannelEventFactory(final List<DistributionChannel<E, D, G>> distributionChannels) {
        this.distributionChannels = distributionChannels;
    }

    public AbstractChannelEvent createEvent(final Long id, final String distributionType, final ProjectData projectData) {
        for (final DistributionChannel<E, D, G> channel : distributionChannels) {
            final AbstractChannelEvent event = channel.createChannelEvent(projectData, id);
            if (event.isApplicable(distributionType)) {
                return event;
            }
        }
        return null;
    }

}
