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

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.slack.parser.SlackChannelEventParser;
import com.synopsys.integration.alert.channel.slack.parser.SlackChannelMessageParser;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.request.Request;

public class SlackChannelTest extends ChannelTest {
    private static final String TEST_WEBHOOK_URL = "https://webhook";
    private static final SlackChannelKey CHANNEL_KEY = new SlackChannelKey();

    private SlackChannel createSlackChannel() {
        RestChannelUtility restChannelUtility = createRestChannelUtility();
        SlackChannelMessageParser slackChannelMessageParser = new SlackChannelMessageParser(new MarkupEncoderUtil());
        SlackChannelEventParser slackChannelEventParser = new SlackChannelEventParser(slackChannelMessageParser, restChannelUtility);
        return new SlackChannel(CHANNEL_KEY, gson, createAuditUtility(), restChannelUtility, slackChannelEventParser);
    }

    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        SlackChannel slackChannel = createSlackChannel();

        ProviderMessageContent messageContent = createMessageContent(getClass().getSimpleName() + ": Request");

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK));
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME));
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME));

        FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        DistributionEvent event = new DistributionEvent(
            "1L", CHANNEL_KEY.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        slackChannel.sendAuditedMessage(event);

        boolean actual = outputLogger.isLineContainingText("Successfully sent a " + CHANNEL_KEY.getUniversalKey() + " message!");
        assertTrue(actual, "No success message appeared in the logs");
    }

    @Test
    public void testCreateRequestMissingWebhook() {
        SlackChannel slackChannel = createSlackChannel();

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);

        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);

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
        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, TEST_WEBHOOK_URL);
        FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);

        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);

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
        }
    }

    @Test
    public void testCreateRequestMissingContent() {
        FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of(TEST_WEBHOOK_URL));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
        ProviderMessageContent content = Mockito.mock(ProviderMessageContent.class);
        Mockito.when(content.getTopic()).thenReturn(Mockito.mock(LinkableItem.class));
        DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
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
        FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of(TEST_WEBHOOK_URL));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
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
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
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
        FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of(TEST_WEBHOOK_URL));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
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
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
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
        FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of(TEST_WEBHOOK_URL));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
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
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
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
        FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of(TEST_WEBHOOK_URL));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
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
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
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
    public void testCreateRequestExceptions() throws Exception {
        SlackChannelMessageParser slackChannelMessageParser = new SlackChannelMessageParser(new MarkupEncoderUtil());
        SlackChannelEventParser slackChannelEventParser = new SlackChannelEventParser(slackChannelMessageParser, null);
        SlackChannel slackChannel = new SlackChannel(CHANNEL_KEY, gson, null, null, slackChannelEventParser);
        List<Request> request = null;

        LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        ProviderMessageContent messageContent = new ProviderMessageContent.Builder()
                                                    .applyProvider("testProvider", 1L, "testProviderConfig")
                                                    .applyTopic("testTopic", "")
                                                    .applySubTopic(subTopic.getName(), subTopic.getValue())
                                                    .build();

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, "");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, "");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, "ChannelUsername");

        FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        DistributionEvent event = new DistributionEvent(
            "1L", CHANNEL_KEY.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        try {
            request = slackChannel.createRequests(event);
            fail();
        } catch (IntegrationException e) {
            assertNull(request, "Expected the request to be null");
        }

        fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, TEST_WEBHOOK_URL);
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, "");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, "ChannelUsername");

        fieldAccessor = new FieldAccessor(fieldModels);
        event = new DistributionEvent(
            "1L", CHANNEL_KEY.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        try {
            request = slackChannel.createRequests(event);
            fail();
        } catch (IntegrationException e) {
            assertNull(request, "Expected the request to be null");
        }
    }

    @Test
    public void testCreateHtmlMessage() throws IntegrationException {
        RestChannelUtility restChannelUtility = new RestChannelUtility(null);
        RestChannelUtility restChannelUtilitySpy = Mockito.spy(restChannelUtility);
        Mockito.doNothing().when(restChannelUtilitySpy).sendMessage(Mockito.any(), Mockito.anyString());
        SlackChannelMessageParser slackChannelMessageParser = new SlackChannelMessageParser(new MarkupEncoderUtil());
        SlackChannelEventParser slackChannelEventParser = new SlackChannelEventParser(slackChannelMessageParser, restChannelUtilitySpy);
        SlackChannel slackChannel = new SlackChannel(CHANNEL_KEY, gson, null, restChannelUtilitySpy, slackChannelEventParser);
        ProviderMessageContent messageContent = createMessageContent(getClass().getSimpleName() + ": Request");

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, TEST_WEBHOOK_URL);
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, "ChannelName");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, "ChannelUsername");

        FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        DistributionEvent event = new DistributionEvent(
            "1L", CHANNEL_KEY.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        slackChannel.sendMessage(event);

        //        assertFalse(request.isEmpty());
        Mockito.verify(restChannelUtilitySpy).sendMessage(Mockito.any(), Mockito.anyString());
    }

    @Test
    public void testCreateHtmlMessageEmpty() throws IntegrationException {
        SlackChannelMessageParser slackChannelMessageParser = new SlackChannelMessageParser(new MarkupEncoderUtil());
        SlackChannelEventParser slackChannelEventParser = new SlackChannelEventParser(slackChannelMessageParser, null);
        SlackChannel slackChannel = new SlackChannel(CHANNEL_KEY, gson, null, null, slackChannelEventParser);

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, TEST_WEBHOOK_URL);
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, "ChannelName");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, "ChannelUsername");

        FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        DistributionEvent event = new DistributionEvent(
            "1L", CHANNEL_KEY.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, ProcessingType.DEFAULT.name(), new MessageContentGroup(), fieldAccessor);
        SlackChannel spySlackChannel = Mockito.spy(slackChannel);
        List<Request> requests = slackChannel.createRequests(event);
        assertTrue(requests.isEmpty(), "Expected no requests to be created");
        Mockito.verify(spySlackChannel, Mockito.times(0)).sendMessage(Mockito.any());
    }

}
