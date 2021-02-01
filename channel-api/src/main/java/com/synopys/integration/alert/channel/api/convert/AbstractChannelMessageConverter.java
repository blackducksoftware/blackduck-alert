/*
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
package com.synopys.integration.alert.channel.api.convert;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.ListUtils;

import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public abstract class AbstractChannelMessageConverter<D extends DistributionJobDetailsModel, T> implements ChannelMessageConverter<D, T> {
    private final SimpleMessageConverter simpleMessageConverter;
    private final ProjectMessageConverter projectMessageConverter;

    protected AbstractChannelMessageConverter(ChannelMessageFormatter channelMessageFormatter) {
        this(new SimpleMessageConverter(channelMessageFormatter), new ProjectMessageConverter(channelMessageFormatter));
    }

    protected AbstractChannelMessageConverter(SimpleMessageConverter simpleMessageConverter, ProjectMessageConverter projectMessageConverter) {
        this.simpleMessageConverter = simpleMessageConverter;
        this.projectMessageConverter = projectMessageConverter;
    }

    @Override
    public final List<T> convertToChannelMessages(D distributionDetails, ProviderMessageHolder messages) {
        List<T> convertedSimpleMessages = convertProviderMessagesToChannelMessages(
            messages.getSimpleMessages(),
            simpleMessageConverter::convertToFormattedMessageChunks,
            (message, formattedMessageChunks) -> convertSimpleMessageToChannelMessages(distributionDetails, message, formattedMessageChunks)
        );

        List<T> convertedProjectMessages = convertProviderMessagesToChannelMessages(
            messages.getProjectMessages(),
            projectMessageConverter::convertToFormattedMessageChunks,
            (message, formattedMessageChunks) -> convertProjectMessageToChannelMessages(distributionDetails, message, formattedMessageChunks)
        );

        return ListUtils.union(convertedSimpleMessages, convertedProjectMessages);
    }

    protected abstract List<T> convertSimpleMessageToChannelMessages(D distributionDetails, SimpleMessage simpleMessage, List<String> messageChunks);

    protected abstract List<T> convertProjectMessageToChannelMessages(D distributionDetails, ProjectMessage projectMessage, List<String> messageChunks);

    private <M extends ProviderMessage<M>> List<T> convertProviderMessagesToChannelMessages(List<M> messages, Function<M, List<String>> convertToMessageChunks, BiFunction<M, List<String>, List<T>> convertToChannelMessages) {
        List<T> channelMessages = new LinkedList<>();
        for (M message : messages) {
            List<String> projectMessageChunks = convertToMessageChunks.apply(message);
            List<T> projectChannelMessages = convertToChannelMessages.apply(message, projectMessageChunks);
            channelMessages.addAll(projectChannelMessages);
        }
        return channelMessages;
    }

}
