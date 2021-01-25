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

import java.util.List;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.detail.ProviderMessageHolder;
import com.synopys.integration.alert.channel.api.convert.AbstractChannelMessageConverter;

public abstract class MessageBoardChannel<D extends DistributionJobDetailsModel, T> implements DistributionChannelV2<D> {
    private final AbstractChannelMessageConverter<D, T> channelMessageConverter;
    private final ChannelMessageSender<D, T, MessageResult> channelMessageSender;

    protected MessageBoardChannel(AbstractChannelMessageConverter<D, T> channelMessageConverter, ChannelMessageSender<D, T, MessageResult> channelMessageSender) {
        this.channelMessageConverter = channelMessageConverter;
        this.channelMessageSender = channelMessageSender;
    }

    @Override
    public MessageResult distributeMessages(D distributionDetails, ProviderMessageHolder messages) throws AlertException {
        List<T> channelMessages = channelMessageConverter.convertToChannelMessages(distributionDetails, messages);
        return channelMessageSender.sendMessages(distributionDetails, channelMessages);
    }

}
