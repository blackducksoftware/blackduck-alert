/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.channel.message.MessageSplitter;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Component
@Deprecated
public class SlackChannelEventParser {
    public static final String SLACK_DEFAULT_USERNAME = "Alert";

    private static final int MRKDWN_MAX_SIZE_PRE_SPLIT = 3500;

    private final SlackChannelMessageParser slackChannelMessageParser;
    private final RestChannelUtility restChannelUtility;

    @Autowired
    public SlackChannelEventParser(SlackChannelMessageParser slackChannelMessageParser, RestChannelUtility restChannelUtility) {
        this.slackChannelMessageParser = slackChannelMessageParser;
        this.restChannelUtility = restChannelUtility;
    }

    public List<Request> createRequests(DistributionEvent event) throws IntegrationException {
        DistributionJobModel distributionJobModel = event.getDistributionJobModel();
        DistributionJobDetailsModel distributionJobDetails = distributionJobModel.getDistributionJobDetails();
        SlackJobDetailsModel slackJobDetails = distributionJobDetails.getAs(DistributionJobDetailsModel.SLACK);

        String webhook = slackJobDetails.getWebhook();
        String channelName = slackJobDetails.getChannelName();

        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        if (StringUtils.isBlank(webhook)) {
            fieldErrors.add(AlertFieldStatus.error(SlackDescriptor.KEY_WEBHOOK, "Missing Webhook URL"));
        }
        if (StringUtils.isBlank(channelName)) {
            fieldErrors.add(AlertFieldStatus.error(SlackDescriptor.KEY_CHANNEL_NAME, "Missing channel name"));
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }

        String channelUsername = Optional.ofNullable(slackJobDetails.getChannelUsername())
                                     .filter(StringUtils::isNotBlank)
                                     .orElse(SLACK_DEFAULT_USERNAME);
        MessageContentGroup eventContent = event.getContent();
        if (!eventContent.isEmpty()) {
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Content-Type", "application/json");

            List<String> mrkdwnMessagePieces = slackChannelMessageParser.createMessagePieces(eventContent);
            return createRequestsForMessage(channelName, channelUsername, webhook, mrkdwnMessagePieces, requestHeaders);
        }
        return List.of();
    }

    private List<Request> createRequestsForMessage(String channelName, String channelUsername, String webhook, List<String> mrkdwnMessagePieces, Map<String, String> requestHeaders) {
        MessageSplitter messageSplitter = new MessageSplitter(MRKDWN_MAX_SIZE_PRE_SPLIT);
        List<String> mrkdwnMessageChunks = messageSplitter.splitMessages(mrkdwnMessagePieces);
        return mrkdwnMessageChunks
                   .stream()
                   .filter(StringUtils::isNotBlank)
                   .map(message -> getJsonString(message, channelName, channelUsername))
                   .map(jsonMessage -> restChannelUtility.createPostMessageRequest(webhook, requestHeaders, jsonMessage))
                   .collect(Collectors.toList());
    }

    private String getJsonString(String htmlMessage, String channel, String username) {
        JsonObject json = new JsonObject();
        json.addProperty("text", htmlMessage);
        json.addProperty("channel", channel);
        json.addProperty("username", username);
        json.addProperty("mrkdwn", true);

        return json.toString();
    }

}
