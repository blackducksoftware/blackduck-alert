/**
 * channel-api
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
package com.synopys.integration.alert.channel.api;

import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.detail.ProviderMessageHolder;

public abstract class DistributionChannelV2<D extends DistributionJobDetailsModel, T, U> {
    protected final ChannelMessageFormatter<T> channelMessageFormatter;
    protected final ChannelMessageSender<T, U> channelMessageSender;

    public DistributionChannelV2(ChannelMessageFormatter<T> channelMessageFormatter, ChannelMessageSender<T, U> channelMessageSender) {
        this.channelMessageFormatter = channelMessageFormatter;
        this.channelMessageSender = channelMessageSender;
    }

    public abstract MessageResult sendMessage(D distributionDetails, ProviderMessageHolder messages);

}
