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
package com.blackducksoftware.integration.alert.common.descriptor;

import java.util.Map;

import javax.jms.MessageListener;

import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.database.RepositoryAccessor;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;

public abstract class ChannelDescriptor extends Descriptor {
    private final String destinationName;
    private final DatabaseContentConverter contentConverter;
    private final RepositoryAccessor repositoryAccessor;

    public ChannelDescriptor(final String name, final String label, final String destinationName, final DatabaseContentConverter globalContentConverter, final RepositoryAccessor globalRepositoryAccessor,
            final DatabaseContentConverter contentConverter,
            final RepositoryAccessor repositoryAccessor) {
        super(name, label, DescriptorType.CHANNEL, globalContentConverter, globalRepositoryAccessor);
        this.destinationName = destinationName;
        this.contentConverter = contentConverter;
        this.repositoryAccessor = repositoryAccessor;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public boolean hasGlobalConfiguration() {
        return getGlobalRepositoryAccessor() != null && getGlobalContentConverter() != null;
    }

    public RepositoryAccessor getDistributionRepositoryAccessor() {
        return repositoryAccessor;
    }

    public DatabaseContentConverter getDistributionContentConverter() {
        return contentConverter;
    }

    public abstract void validateDistributionConfig(CommonDistributionConfigRestModel restModel, Map<String, String> fieldErrors);

    public abstract void testDistributionConfig(CommonDistributionConfigRestModel restModel, ChannelEvent event) throws IntegrationException;

    public abstract MessageListener getChannelListener();
}
