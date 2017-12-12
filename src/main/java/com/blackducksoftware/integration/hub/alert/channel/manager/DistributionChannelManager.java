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
package com.blackducksoftware.integration.hub.alert.channel.manager;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public abstract class DistributionChannelManager<G extends GlobalChannelConfigEntity, D extends DistributionChannelConfigEntity, E extends AbstractChannelEvent> {
    private final DistributionChannel<E, G, D> distributionChannel;
    private final JpaRepository<G, Long> globalRepository;
    private final JpaRepository<D, Long> localRepository;

    public DistributionChannelManager(final DistributionChannel<E, G, D> distributionChannel, final JpaRepository<G, Long> globalRepository, final JpaRepository<D, Long> localRepository) {
        this.distributionChannel = distributionChannel;
        this.globalRepository = globalRepository;
        this.localRepository = localRepository;
    }

    public DistributionChannel<E, G, D> getDistributionChannel() {
        return distributionChannel;
    }

    public JpaRepository<G, Long> getGlobalRepository() {
        return globalRepository;
    }

    public JpaRepository<D, Long> getLocalRepository() {
        return localRepository;
    }

    public abstract boolean isApplicable(final String supportedChannelName);

    public abstract E createChannelEvent(final ProjectData projectData, final Long commonDistributionConfigId);

}
