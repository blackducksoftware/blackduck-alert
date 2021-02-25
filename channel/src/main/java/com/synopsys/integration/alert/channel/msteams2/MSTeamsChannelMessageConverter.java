/*
 * channel
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
package com.synopsys.integration.alert.channel.msteams2;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.convert.AbstractChannelMessageConverter;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

@Component
public class MSTeamsChannelMessageConverter extends AbstractChannelMessageConverter<MSTeamsJobDetailsModel, MSTeamsChannelMessageModel> {
    @Autowired
    protected MSTeamsChannelMessageConverter(MSTeamsChannelMessageFormatter channelMessageFormatter) {
        super(channelMessageFormatter);
    }

    @Override
    protected List<MSTeamsChannelMessageModel> convertSimpleMessageToChannelMessages(MSTeamsJobDetailsModel msTeamsJobDetailsModel, SimpleMessage simpleMessage, List<String> messageChunks) {
        return createMessageModel(simpleMessage.getProviderDetails(), messageChunks);
    }

    @Override
    protected List<MSTeamsChannelMessageModel> convertProjectMessageToChannelMessages(MSTeamsJobDetailsModel msTeamsJobDetailsModel, ProjectMessage projectMessage, List<String> messageChunks) {
        return createMessageModel(projectMessage.getProviderDetails(), messageChunks);
    }

    private List<MSTeamsChannelMessageModel> createMessageModel(ProviderDetails providerDetails, List<String> messageChunks) {
        String provider = providerDetails.getProvider().getValue();
        String messageTitle = String.format("Received a message from %s", provider);
        List<MSTeamsChannelMessageSection> messageSections = createMessageSections(messageChunks);
        MSTeamsChannelMessageModel messageModel = new MSTeamsChannelMessageModel(providerDetails, messageTitle, messageSections);
        return List.of(messageModel);
    }

    private List<MSTeamsChannelMessageSection> createMessageSections(List<String> messageChunks) {
        List<MSTeamsChannelMessageSection> messageSections = new LinkedList<>();
        int messageChunksSize = messageChunks.size();
        
        if (messageChunksSize > 1) {
            for (int i = 0; i < messageChunksSize; i++) {
                String title = String.format("(%s/%s)", i + 1, messageChunksSize);
                messageSections.add(new MSTeamsChannelMessageSection(title, messageChunks.get(i)));
            }
        } else {
            messageChunks.stream()
                .map(messageContent -> new MSTeamsChannelMessageSection(StringUtils.EMPTY, messageContent))
                .forEach(messageSections::add);
        }

        return messageSections;
    }

}
