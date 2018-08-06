/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.alert.channel.rest;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.alert.TestBlackDuckProperties;
import com.blackducksoftware.integration.alert.channel.ChannelTest;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.alert.common.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.alert.database.entity.channel.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.request.Request;
import com.google.gson.Gson;

public class RestDistributionChannelTest extends ChannelTest {
    @Test
    public void sendMessageFailureTest() {
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties();
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(hubProperties);
        final Gson gson = new Gson();
        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity> restChannel = new RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity>(gson, hubProperties, null, null,
                null, null,
                channelRestConnectionFactory, contentConverter) {

            @Override
            public String getApiUrl(final GlobalChannelConfigEntity entity) {
                return null;
            }

            @Override
            public List<Request> createRequests(final ChannelRequestHelper channelRequestHelper, final DistributionChannelConfigEntity config, final GlobalChannelConfigEntity globalConfig, final ChannelEvent event) throws AlertException {
                return Arrays.asList(new Request.Builder().uri("http://google.com").build());
            }
        };
        final DigestModel digestModel = new DigestModel(createProjectData("Rest channel test"));
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent event = new ChannelEvent(SlackChannel.COMPONENT_NAME, RestConnection.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), 1L, 1L);
        final SlackDistributionConfigEntity config = new SlackDistributionConfigEntity("more garbage", "garbage", "garbage");
        Exception thrownException = null;
        try {
            restChannel.sendAuditedMessage(event, config);
        } catch (final IntegrationException ex) {
            thrownException = ex;
        }

        assertNotNull(thrownException);
    }
}
