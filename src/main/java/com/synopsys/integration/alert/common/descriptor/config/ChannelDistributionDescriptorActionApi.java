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
package com.synopsys.integration.alert.common.descriptor.config;

import com.synopsys.integration.alert.channel.DistributionChannel;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class ChannelDistributionDescriptorActionApi extends DescriptorActionApi {
    private final DistributionChannel distributionChannel;

    public ChannelDistributionDescriptorActionApi(final TypeConverter typeConverter, final RepositoryAccessor repositoryAccessor, final DistributionChannel distributionChannel) {
        super(typeConverter, repositoryAccessor);
        this.distributionChannel = distributionChannel;
    }

    public ChannelDistributionDescriptorActionApi(final TypeConverter typeConverter, final RepositoryAccessor repositoryAccessor, final DistributionChannel distributionChannel, final StartupComponent startupComponent) {
        super(typeConverter, repositoryAccessor, startupComponent);
        this.distributionChannel = distributionChannel;
    }

    @Override
    public void testConfig(final TestConfigModel testConfigModel) throws IntegrationException {
        final DistributionEvent event = createChannelTestEvent((CommonDistributionConfig) testConfigModel.getRestModel());
        distributionChannel.sendMessage(event);
    }
}
