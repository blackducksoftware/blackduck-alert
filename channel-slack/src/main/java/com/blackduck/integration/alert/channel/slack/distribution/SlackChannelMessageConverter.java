/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.slack.distribution;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.convert.AbstractChannelMessageConverter;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

@Component
public class SlackChannelMessageConverter extends AbstractChannelMessageConverter<SlackJobDetailsModel, SlackChannelMessageModel> {

    @Autowired
    protected SlackChannelMessageConverter(SlackChannelMessageFormatter channelMessageFormatter) {
        super(channelMessageFormatter);
    }

    @Override
    protected List<SlackChannelMessageModel> convertSimpleMessageToChannelMessages(SlackJobDetailsModel slackJobDetailsModel, SimpleMessage simpleMessage, List<String> messageChunks) {
        return createMessageModel(messageChunks);
    }

    @Override
    protected List<SlackChannelMessageModel> convertProjectMessageToChannelMessages(SlackJobDetailsModel slackJobDetailsModel, ProjectMessage projectMessage, List<String> messageChunks) {
        return createMessageModel(messageChunks);
    }

    private List<SlackChannelMessageModel> createMessageModel(List<String> messageChunks) {
        return messageChunks.stream()
                   .map(SlackChannelMessageModel::new)
                   .collect(Collectors.toList());
    }

}
