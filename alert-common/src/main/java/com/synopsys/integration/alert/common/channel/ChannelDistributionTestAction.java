/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.channel;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class ChannelDistributionTestAction {
    private final DistributionChannel distributionChannel;

    public ChannelDistributionTestAction(DistributionChannel distributionChannel) {
        this.distributionChannel = distributionChannel;
    }

    public MessageResult testConfig(DistributionJobModel testJobModel, @Nullable ConfigurationModel channelGlobalConfig) throws IntegrationException {
        return testConfig(testJobModel, channelGlobalConfig, null, null, null);
    }

    public MessageResult testConfig(
        DistributionJobModel testJobModel,
        @Nullable ConfigurationModel channelGlobalConfig,
        @Nullable String customTopic,
        @Nullable String customMessage,
        @Nullable String destination
    ) throws IntegrationException {
        String topicString = Optional.ofNullable(customTopic).orElse("Alert Test Topic");
        String messageString = Optional.ofNullable(customMessage).orElse("Alert Test Message");
        DistributionEvent channelTestEvent = ChannelDistributionTestEventCreationUtils.createChannelTestEvent(topicString, messageString, testJobModel, channelGlobalConfig);
        return distributionChannel.sendMessage(channelTestEvent);
    }

    public DistributionChannel getDistributionChannel() {
        return distributionChannel;
    }

}
