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
package com.blackducksoftware.integration.hub.alert.descriptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.jms.MessageListener;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public abstract class ChannelDescriptor extends Descriptor {
    private final String destinationName;
    private final boolean hasGlobalConfiguration;

    public ChannelDescriptor(final String name, final String destinationName, final boolean hasGlobalConfiguration) {
        super(name, DescriptorType.CHANNEL);
        this.destinationName = destinationName;
        this.hasGlobalConfiguration = hasGlobalConfiguration;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public boolean hasGlobalConfiguration() {
        return hasGlobalConfiguration;
    }

    public abstract List<? extends DatabaseEntity> readDistributionEntities();

    public abstract Optional<? extends DatabaseEntity> readDistributionEntity(long id);

    public abstract Optional<? extends DatabaseEntity> saveDistributionEntity(DatabaseEntity entity);

    public abstract void deleteDistributionEntity(long id);

    public abstract CommonDistributionConfigRestModel convertFromStringToDistributionRestModel(String json);

    public abstract DatabaseEntity convertFromDistributionRestModelToDistributionConfigEntity(CommonDistributionConfigRestModel restModel) throws AlertException;

    public abstract void validateDistributionConfig(CommonDistributionConfigRestModel restModel, Map<String, String> fieldErrors);

    public abstract Optional<? extends CommonDistributionConfigRestModel> constructRestModel(final CommonDistributionConfigEntity commonEntity, final DatabaseEntity distributionEntity) throws AlertException;

    public abstract void testDistributionConfig(CommonDistributionConfigRestModel restModel, ChannelEvent event) throws IntegrationException;

    public abstract MessageListener getChannelListener();
}
