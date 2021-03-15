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
package com.synopsys.integration.alert.channel.slack2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.AbstractChannelTest;
import com.synopsys.integration.alert.channel.ChannelITTestAssertions;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.parser.SlackChannelEventParser;
import com.synopsys.integration.alert.channel.slack.parser.SlackChannelMessageParser;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

public class SlackChannelTest extends AbstractChannelTest {
    private static final String TEST_WEBHOOK_URL = "https://webhook";

    private SlackChannel createSlackChannel() {
        RestChannelUtility restChannelUtility = createRestChannelUtility();
        SlackChannelMessageParser slackChannelMessageParser = new SlackChannelMessageParser(new MarkupEncoderUtil());
        SlackChannelEventParser slackChannelEventParser = new SlackChannelEventParser(slackChannelMessageParser, restChannelUtility);
        return new SlackChannel(gson, auditAccessor, restChannelUtility, slackChannelEventParser);
    }

    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() {
        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();
        SlackChannelMessageFormatter slackChannelMessageFormatter = new SlackChannelMessageFormatter(markupEncoderUtil);
        SlackChannelMessageConverter slackChannelMessageConverter = new SlackChannelMessageConverter(slackChannelMessageFormatter);

        SlackChannelMessageSender slackChannelMessageSender = new SlackChannelMessageSender(createRestChannelUtility(), ChannelKeys.SLACK);

        SlackChannelV2 slackChannel = new SlackChannelV2(slackChannelMessageConverter, slackChannelMessageSender);

        SlackJobDetailsModel distributionDetails = new SlackJobDetailsModel(
            null,
            properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK),
            properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME),
            properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME)
        );

        ChannelITTestAssertions.assertSendSimpleMessageSuccess(slackChannel, distributionDetails);
    }

    @Test
    public void testCreateRequestMissingWebhook() {
        SlackChannel slackChannel = createSlackChannel();

        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        DistributionJobModel testJobModel = createTestJobModel(
            null,
            "not empty",
            "not empty"
        );
        Mockito.when(event.getDistributionJobModel()).thenReturn(testJobModel);

        ProviderMessageContent content = Mockito.mock(ProviderMessageContent.class);
        Mockito.when(content.getTopic()).thenReturn(new LinkableItem("topic", "topicVal"));

        MessageContentGroup contentGroup = MessageContentGroup.singleton(content);
        LinkableItem topicItem = Mockito.mock(LinkableItem.class);
        Mockito.when(topicItem.getValue()).thenReturn("Value");
        Mockito.when(content.getTopic()).thenReturn(topicItem);
        Mockito.when(event.getContent()).thenReturn(contentGroup);
        try {
            slackChannel.createRequests(event);
            fail("Expected an exception for missing webhook");
        } catch (IntegrationException e) {
        }
    }

    @Test
    public void testCreateRequestMissingChannelName() {
        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        DistributionJobModel testJobModel = createTestJobModel(
            TEST_WEBHOOK_URL,
            null,
            "not empty"
        );
        Mockito.when(event.getDistributionJobModel()).thenReturn(testJobModel);

        ProviderMessageContent content = Mockito.mock(ProviderMessageContent.class);
        Mockito.when(content.getTopic()).thenReturn(new LinkableItem("topic", "topicVal"));

        MessageContentGroup contentGroup = MessageContentGroup.singleton(content);
        LinkableItem topicItem = Mockito.mock(LinkableItem.class);
        Mockito.when(topicItem.getValue()).thenReturn("Value");
        Mockito.when(content.getTopic()).thenReturn(topicItem);
        Mockito.when(event.getContent()).thenReturn(contentGroup);

        SlackChannel slackChannel = createSlackChannel();

        try {
            slackChannel.createRequests(event);
            fail("Expected an exception for missing channel name");
        } catch (IntegrationException e) {
            // Pass
        }
    }

    @Test
    public void testCreateRequestMissingContent() {
        ProviderMessageContent content = Mockito.mock(ProviderMessageContent.class);
        Mockito.when(content.getTopic()).thenReturn(Mockito.mock(LinkableItem.class));
        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        DistributionJobModel testJobModel = createTestJobModel(
            TEST_WEBHOOK_URL,
            "slack_channel",
            "user_name"
        );
        Mockito.when(event.getDistributionJobModel()).thenReturn(testJobModel);

        MessageContentGroup contentGroup = MessageContentGroup.singleton(content);
        Mockito.when(event.getContent()).thenReturn(contentGroup);

        SlackChannel slackChannel = createSlackChannel();

        try {
            assertTrue(slackChannel.createRequests(event).isEmpty(), "Expected no requests to be created");
        } catch (IntegrationException e) {
        }
    }

    @Test
    public void testCreateRequestSingleCategory() throws Exception {
        SortedSet<LinkableItem> items = new TreeSet<>();
        items.add(new LinkableItem("itemName", "itemvalue"));
        ComponentItem componentItem = new ComponentItem.Builder()
                                          .applyCategory("category")
                                          .applyOperation(ItemOperation.ADD)
                                          .applyComponentData("", "")
                                          .applyCategoryItem("", "")
                                          .applyNotificationId(1L)
                                          .applyAllComponentAttributes(items)
                                          .build();

        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("Message Content", "Slack Unit Test from Alert")
                                             .applyComponentItem(componentItem)
                                             .build();
        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        DistributionJobModel testJobModel = createTestJobModel(
            TEST_WEBHOOK_URL,
            "slack_channel",
            "user_name"
        );
        Mockito.when(event.getDistributionJobModel()).thenReturn(testJobModel);
        Mockito.when(event.getContent()).thenReturn(MessageContentGroup.singleton(content));

        SlackChannel slackChannel = createSlackChannel();

        List<Request> requests = slackChannel.createRequests(event);
        assertFalse(requests.isEmpty(), "Expected requests to be created");
        assertEquals(1, requests.size());
        Request actualRequest = requests.get(0);
        assertEquals(TEST_WEBHOOK_URL, actualRequest.getUrl().toString());
        assertNotNull(actualRequest.getBodyContent(), "Expected the body content to be set");
    }

    @Test
    public void testCreateRequestSingleCategoryWithItemUrl() throws Exception {
        SortedSet<LinkableItem> items = new TreeSet<>();
        items.add(new LinkableItem("itemName", "itemvalue", "url"));

        ComponentItem componentItem = new ComponentItem.Builder()
                                          .applyCategory("category")
                                          .applyOperation(ItemOperation.ADD)
                                          .applyComponentData("", "")
                                          .applyCategoryItem("", "")
                                          .applyNotificationId(1L)
                                          .applyAllComponentAttributes(items)
                                          .build();

        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("Message Content", "Slack Unit Test from Alert")
                                             .applyComponentItem(componentItem)
                                             .build();
        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        DistributionJobModel testJobModel = createTestJobModel(
            TEST_WEBHOOK_URL,
            "slack_channel",
            "user_name"
        );
        Mockito.when(event.getDistributionJobModel()).thenReturn(testJobModel);
        Mockito.when(event.getContent()).thenReturn(MessageContentGroup.singleton(content));

        SlackChannel slackChannel = createSlackChannel();

        List<Request> requests = slackChannel.createRequests(event);
        assertFalse(requests.isEmpty(), "Expected requests to be created");
        assertEquals(1, requests.size());
        Request actualRequest = requests.get(0);
        assertEquals(TEST_WEBHOOK_URL, actualRequest.getUrl().toString());
        assertNotNull(actualRequest.getBodyContent(), "Expected the body content to be set");
    }

    @Test
    public void testCreateRequestMultipleCategory() throws Exception {
        SortedSet<LinkableItem> items = new TreeSet<>();
        items.add(new LinkableItem("itemName", "itemvalue_1"));
        items.add(new LinkableItem("itemName", "itemvalue_2"));

        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyCategory("category")
                                            .applyOperation(ItemOperation.ADD)
                                            .applyComponentData("", "")
                                            .applyCategoryItem("", "")
                                            .applyNotificationId(1L)
                                            .applyAllComponentAttributes(items)
                                            .build();

        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyCategory("category")
                                            .applyOperation(ItemOperation.ADD)
                                            .applyComponentData("", "")
                                            .applyCategoryItem("", "")
                                            .applyNotificationId(2L)
                                            .applyAllComponentAttributes(items)
                                            .build();

        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("Message Content", "Slack Unit Test from Alert")
                                             .applyAllComponentItems(List.of(componentItem_1, componentItem_2))
                                             .build();
        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        DistributionJobModel testJobModel = createTestJobModel(
            TEST_WEBHOOK_URL,
            "slack_channel",
            "user_name"
        );
        Mockito.when(event.getDistributionJobModel()).thenReturn(testJobModel);
        Mockito.when(event.getContent()).thenReturn(MessageContentGroup.singleton(content));

        SlackChannel slackChannel = createSlackChannel();

        List<Request> requests = slackChannel.createRequests(event);
        assertFalse(requests.isEmpty(), "Expected requests to be created");
        assertEquals(1, requests.size());
        Request actualRequest = requests.get(0);
        assertEquals(TEST_WEBHOOK_URL, actualRequest.getUrl().toString());
        assertNotNull(actualRequest.getBodyContent(), "Expected the body content to be set");
    }

    @Test
    public void testCreateRequestMultipleCategoryWithItemUrls() throws Exception {
        SortedSet<LinkableItem> items = new TreeSet<>();
        items.add(new LinkableItem("itemName", "itemvalue_1", "itemUrl"));
        items.add(new LinkableItem("itemName", "itemvalue_2", "itemUrl"));
        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyCategory("category")
                                            .applyOperation(ItemOperation.ADD)
                                            .applyComponentData("", "")
                                            .applyCategoryItem("", "")
                                            .applyNotificationId(1L)
                                            .applyAllComponentAttributes(items)
                                            .build();

        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyCategory("category")
                                            .applyOperation(ItemOperation.ADD)
                                            .applyComponentData("", "")
                                            .applyCategoryItem("", "")
                                            .applyNotificationId(2L)
                                            .applyAllComponentAttributes(items)
                                            .build();

        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("Message Content", "Slack Unit Test from Alert")
                                             .applyAllComponentItems(List.of(componentItem_1, componentItem_2))
                                             .build();
        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        DistributionJobModel testJobModel = createTestJobModel(
            TEST_WEBHOOK_URL,
            "slack_channel",
            "user_name"
        );
        Mockito.when(event.getDistributionJobModel()).thenReturn(testJobModel);
        Mockito.when(event.getContent()).thenReturn(MessageContentGroup.singleton(content));

        SlackChannel slackChannel = createSlackChannel();

        List<Request> requests = slackChannel.createRequests(event);
        assertFalse(requests.isEmpty(), "Expected requests to be created");
        assertEquals(1, requests.size());
        Request actualRequest = requests.get(0);
        assertEquals(TEST_WEBHOOK_URL, actualRequest.getUrl().toString());
        assertNotNull(actualRequest.getBodyContent(), "Expected the body content to be set");
    }

    private DistributionJobModel createTestJobModel(String webhook, String channelName, String channelUsername) {
        SlackJobDetailsModel jobDetailsModel = new SlackJobDetailsModel(null, webhook, channelName, channelUsername);
        return DistributionJobModel.builder()
                   .distributionJobDetails(jobDetailsModel)
                   .build();
    }

}
