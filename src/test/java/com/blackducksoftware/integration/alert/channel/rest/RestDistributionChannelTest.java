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

import org.junit.Test;

import com.blackducksoftware.integration.alert.TestGlobalProperties;
import com.blackducksoftware.integration.alert.channel.ChannelTest;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRequestHelper;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.alert.channel.rest.RestDistributionChannel;
import com.blackducksoftware.integration.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.datasource.entity.channel.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.alert.datasource.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.rest.request.Request;
import com.google.gson.Gson;

public class RestDistributionChannelTest extends ChannelTest {
    @Test
    public void sendMessageFailureTest() {
        final GlobalProperties globalProperties = new TestGlobalProperties();
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(globalProperties);
        final Gson gson = new Gson();
        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity> restChannel = new RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity>(gson, globalProperties, null, null,
                null, null,
                channelRestConnectionFactory, contentConverter) {

            @Override
            public String getApiUrl(final GlobalChannelConfigEntity entity) {
                return null;
            }

            @Override
            public Request createRequest(final ChannelRequestHelper channelRequestHelper, final DistributionChannelConfigEntity config, final GlobalChannelConfigEntity globalConfig, final DigestModel digestModel) throws AlertException {
                return new Request.Builder().uri("http://google.com").build();
            }
        };
        final DigestModel digestModel = new DigestModel(createProjectData("Rest channel test"));
        final ChannelEvent event = new ChannelEvent(SlackChannel.COMPONENT_NAME, contentConverter.getStringValue(digestModel), 1L);
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
