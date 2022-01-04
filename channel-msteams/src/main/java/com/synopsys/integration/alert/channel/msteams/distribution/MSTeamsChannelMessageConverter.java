/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.distribution;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.convert.AbstractChannelMessageConverter;
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
        MSTeamsChannelMessageModel messageModel = new MSTeamsChannelMessageModel(messageTitle, messageSections);
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
