/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.channel.slack;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.TestTags;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.request.Request;

public class SlackChannelTestIT extends ChannelTest {

    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(new Gson(), mockedGlobalRepository, testAlertProperties, null);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final SlackChannel slackChannel = new SlackChannel(gson, testAlertProperties, globalProperties, auditUtility, channelRestConnectionFactory);
        final String roomName = properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME);
        final String username = properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME);
        final String webHook = properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK);

        final AggregateMessageContent content = createMessageContent(getClass().getSimpleName());
        final SlackChannelEvent event = new SlackChannelEvent(RestConstants.formatDate(new Date()), "provider", "FORMAT", content, new Long(0), username, webHook, roomName);

        slackChannel.sendAuditedMessage(event);

        final boolean actual = outputLogger.isLineContainingText("Successfully sent a " + SlackChannel.COMPONENT_NAME + " message!");
        assertTrue(actual);
    }

    @Test
    public void testCreateRequestExceptions() {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null);
        List<Request> request = null;

        SlackChannelEvent event = new SlackChannelEvent(null, null, null, null, null, "ChannelUsername", "", "");
        try {
            request = slackChannel.createRequests(null, event);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request);
        }

        event = new SlackChannelEvent(null, null, null, null,
                null, "ChannelUsername", "Webhook", "");
        try {
            request = slackChannel.createRequests(null, event);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request);
        }
    }

    @Test
    public void testCreateHtmlMessage() throws IntegrationException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final SlackChannel slackChannel = new SlackChannel(gson, testAlertProperties, null, null, null);
        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());

        final SlackChannelEvent event = new SlackChannelEvent(RestConstants.formatDate(new Date()), "provider", "FORMAT",
                content, new Long(0), "ChannelUsername", "Webhook", "ChannelName");

        final SlackChannel spySlackChannel = Mockito.spy(slackChannel);
        final List<Request> request = spySlackChannel.createRequests(null, event);

        assertFalse(request.isEmpty());
        Mockito.verify(spySlackChannel).createPostMessageRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
    }

    @Test
    public void testCreateHtmlMessageEmpty() throws IntegrationException {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null);
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());

        final SlackChannelEvent event = new SlackChannelEvent(RestConstants.formatDate(new Date()), "provider", "FORMAT", content, new Long(0), "ChannelUsername", "Webhook", "ChannelName");

        final SlackChannel spySlackChannel = Mockito.spy(slackChannel);
        final List<Request> requests = slackChannel.createRequests(null, event);
        assertTrue(requests.isEmpty());
        Mockito.verify(spySlackChannel, Mockito.times(0)).createPostMessageRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
    }
}
