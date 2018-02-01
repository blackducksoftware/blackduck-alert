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
package com.blackducksoftware.integration.hub.alert.channel.rest;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTest;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackEvent;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;

import okhttp3.Request;

public class RestDistributionChannelTest extends ChannelTest {
    @Test
    public void sendMessageFailureTest() {
        final GlobalProperties globalProperties = new TestGlobalProperties();
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(globalProperties);
        final RestDistributionChannel<AbstractChannelEvent, GlobalChannelConfigEntity, DistributionChannelConfigEntity> restChannel = new RestDistributionChannel<AbstractChannelEvent, GlobalChannelConfigEntity, DistributionChannelConfigEntity>(
                null, null, null, null, null, null, channelRestConnectionFactory) {
            @Override
            public String getApiUrl() {
                return null;
            }

            @Override
            public Request createRequest(final ChannelRequestHelper channelRequestHelper, final DistributionChannelConfigEntity config, final ProjectData projectData) throws AlertException {
                return new Request.Builder().url("http://google.com").delete().build();
            }
        };

        final SlackEvent event = new SlackEvent(createProjectData("Rest channel test"), 1L);
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
