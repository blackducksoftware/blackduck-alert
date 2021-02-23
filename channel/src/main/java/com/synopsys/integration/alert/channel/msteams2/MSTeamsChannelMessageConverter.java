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
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

@Component
public class MSTeamsChannelMessageConverter extends AbstractChannelMessageConverter<MSTeamsJobDetailsModel, MSTeamsChannelMessageModel> {
    @Autowired
    protected MSTeamsChannelMessageConverter(MSTeamsChannelMessageFormatter channelMessageFormatter) {
        super(channelMessageFormatter);
    }

    @Override
    protected List<MSTeamsChannelMessageModel> convertSimpleMessageToChannelMessages(MSTeamsJobDetailsModel msTeamsJobDetailsModel, SimpleMessage simpleMessage, List<String> messageChunks) {
        String sectionTitle = simpleMessage.getDescription(); // TODO: Not sure what the topic of a SimpleMessage is
        return createMessageModel(simpleMessage.getProviderDetails(), sectionTitle, messageChunks);
    }

    @Override
    protected List<MSTeamsChannelMessageModel> convertProjectMessageToChannelMessages(MSTeamsJobDetailsModel msTeamsJobDetailsModel, ProjectMessage projectMessage, List<String> messageChunks) {
        boolean wasDeleted = projectMessage.getOperation()
                                 .filter(ProjectOperation.DELETE::equals)
                                 .isPresent();
        LinkableItem project = projectMessage.getProject();
        String projectTitle = createProjectTitle(project, wasDeleted);
        String projectVersionTitle = projectMessage.getProjectVersion()
                                         .map(it -> createProjectTitle(it, wasDeleted))
                                         .orElse(StringUtils.EMPTY);

        String sectionTitle = projectTitle + projectVersionTitle;

        return createMessageModel(projectMessage.getProviderDetails(), sectionTitle, messageChunks);
    }

    private String createProjectTitle(LinkableItem linkableItem, boolean wasDeleted) {
        if (wasDeleted) {
            linkableItem = new LinkableItem(linkableItem.getLabel(), linkableItem.getValue());
        }
        return projectMessageConverter.createLinkableItemString(linkableItem, true);
    }

    private List<MSTeamsChannelMessageModel> createMessageModel(ProviderDetails providerDetails, String sectionTitle, List<String> messageChunks) {
        List<MSTeamsChannelMessageModel> messageModels = new LinkedList<>();
        int messagesSize = messageChunks.size();
        if (messagesSize > 1) {
            for (int i = 0; i < messagesSize; i++) {
                String newTitle = String.format("%s (%s/%s)", sectionTitle, i + 1, messagesSize);
                messageModels.add(new MSTeamsChannelMessageModel(providerDetails, newTitle, messageChunks.get(i)));
            }
        } else {
            messageChunks.stream()
                .map(messageContent -> new MSTeamsChannelMessageModel(providerDetails, sectionTitle, messageContent))
                .forEach(messageModels::add);
        }

        return messageModels;
    }

}
