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

import com.google.gson.Gson;
import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.rest.RestChannelUtility;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;

public class SlackChannelTest extends ChannelTest {

    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties, proxyManager);
        final RestChannelUtility restChannelUtility = new RestChannelUtility(channelRestConnectionFactory);
        final SlackChannel slackChannel = new SlackChannel(gson, auditUtility, restChannelUtility);

        final AggregateMessageContent messageContent = createMessageContent(getClass().getSimpleName() + ": Request");

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK));
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME));
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME));

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        final DistributionEvent event = new DistributionEvent(
            "1L", SlackChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        slackChannel.sendAuditedMessage(event);

        final boolean actual = outputLogger.isLineContainingText("Successfully sent a " + SlackChannel.COMPONENT_NAME + " message!");
        assertTrue(actual, "No success message appeared in the logs");
    }

    @Test
    public void testCreateRequestMissingWebhook() {
        final ChannelRestConnectionFactory channelRestConnectionFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final RestChannelUtility restChannelUtility = new RestChannelUtility(channelRestConnectionFactory);
        final SlackChannel channel = new SlackChannel(new Gson(), auditUtility, restChannelUtility);

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);

        final DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);

        final AggregateMessageContent content = Mockito.mock(AggregateMessageContent.class);
        final MessageContentGroup contentGroup = MessageContentGroup.singleton(content);
        Mockito.when(content.getValue()).thenReturn("Value");
        Mockito.when(event.getContent()).thenReturn(contentGroup);
        try {
            channel.createRequests(event);
            fail("Expected an exception for missing webhook");
        } catch (final IntegrationException e) {
        }
    }

    @Test
    public void testCreateRequestMissingChannelName() {
        final ChannelRestConnectionFactory channelRestConnectionFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, "webhook");
        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);

        final DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);

        final AggregateMessageContent content = Mockito.mock(AggregateMessageContent.class);
        final MessageContentGroup contentGroup = MessageContentGroup.singleton(content);
        Mockito.when(content.getValue()).thenReturn("Value");
        Mockito.when(event.getContent()).thenReturn(contentGroup);

        final RestChannelUtility restChannelUtility = new RestChannelUtility(channelRestConnectionFactory);
        final SlackChannel channel = new SlackChannel(new Gson(), auditUtility, restChannelUtility);

        try {
            channel.createRequests(event);
            fail("Expected an exception for missing channel name");
        } catch (final IntegrationException e) {
        }
    }

    @Test
    public void testCreateRequestMissingContent() throws Exception {
        final ChannelRestConnectionFactory channelRestConnectionFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of("webhook"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
        final AggregateMessageContent content = Mockito.mock(AggregateMessageContent.class);
        Mockito.when(content.getValue()).thenReturn(null);
        final DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
        final MessageContentGroup contentGroup = MessageContentGroup.singleton(content);
        Mockito.when(event.getContent()).thenReturn(contentGroup);

        final RestChannelUtility restChannelUtility = new RestChannelUtility(channelRestConnectionFactory);
        final SlackChannel channel = new SlackChannel(new Gson(), auditUtility, restChannelUtility);

        try {
            assertTrue(channel.createRequests(event).isEmpty(), "Expected no requests to be created");
        } catch (final IntegrationException e) {
        }
    }

    @Test
    public void testCreateRequestSingleCategory() throws Exception {
        final ChannelRestConnectionFactory channelRestConnectionFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of("webhook"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
        final SortedSet<LinkableItem> items = new TreeSet<>();
        items.add(new LinkableItem("itemName", "itemvalue"));
        final CategoryItem categoryItem = new CategoryItem(CategoryKey.from("type", "Key"), ItemOperation.ADD, 1L, items);

        final SortedSet<CategoryItem> categoryItems = new TreeSet<>();
        categoryItems.add(categoryItem);
        final AggregateMessageContent content = new AggregateMessageContent("Message Content", "Slack Unit Test from Alert", categoryItems);
        final DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
        Mockito.when(event.getContent()).thenReturn(MessageContentGroup.singleton(content));

        final RestChannelUtility restChannelUtility = new RestChannelUtility(channelRestConnectionFactory);
        final SlackChannel channel = new SlackChannel(new Gson(), auditUtility, restChannelUtility);

        final List<Request> requests = channel.createRequests(event);
        assertFalse(requests.isEmpty(), "Expected requests to be created");
        assertEquals(1, requests.size());
        final Request actualRequest = requests.get(0);
        assertEquals("webhook", actualRequest.getUri());
        assertNotNull(actualRequest.getBodyContent(), "Expected the body content to be set");
    }

    @Test
    public void testCreateRequestSingleCategoryWithItemUrl() throws Exception {
        final ChannelRestConnectionFactory channelRestConnectionFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of("webhook"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
        final SortedSet<LinkableItem> items = new TreeSet<>();
        items.add(new LinkableItem("itemName", "itemvalue", "url"));
        final CategoryItem categoryItem = new CategoryItem(CategoryKey.from("type", "Key"), ItemOperation.ADD, 1L, items);

        final SortedSet<CategoryItem> categoryItems = new TreeSet<>();
        categoryItems.add(categoryItem);
        final AggregateMessageContent content = new AggregateMessageContent("Message Content", "Slack Unit Test from Alert", categoryItems);
        final DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
        Mockito.when(event.getContent()).thenReturn(MessageContentGroup.singleton(content));

        final RestChannelUtility restChannelUtility = new RestChannelUtility(channelRestConnectionFactory);
        final SlackChannel channel = new SlackChannel(new Gson(), auditUtility, restChannelUtility);

        final List<Request> requests = channel.createRequests(event);
        assertFalse(requests.isEmpty(), "Expected requests to be created");
        assertEquals(1, requests.size());
        final Request actualRequest = requests.get(0);
        assertEquals("webhook", actualRequest.getUri());
        assertNotNull(actualRequest.getBodyContent(), "Expected the body content to be set");
    }

    @Test
    public void testCreateRequestMultipleCategory() throws Exception {
        final ChannelRestConnectionFactory channelRestConnectionFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of("webhook"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
        final SortedSet<LinkableItem> items = new TreeSet<>();
        items.add(new LinkableItem("itemName", "itemvalue_1"));
        items.add(new LinkableItem("itemName", "itemvalue_2"));
        final CategoryItem categoryItem_1 = new CategoryItem(CategoryKey.from("type", "Key1"), ItemOperation.ADD, 1L, items);
        final CategoryItem categoryItem_2 = new CategoryItem(CategoryKey.from("type", "Key2"), ItemOperation.ADD, 2L, items);

        final SortedSet<CategoryItem> categoryItems = new TreeSet<>();
        categoryItems.add(categoryItem_1);
        categoryItems.add(categoryItem_2);
        final AggregateMessageContent content = new AggregateMessageContent("Message Content", "Slack Unit Test from Alert", categoryItems);
        final DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
        Mockito.when(event.getContent()).thenReturn(MessageContentGroup.singleton(content));

        final RestChannelUtility restChannelUtility = new RestChannelUtility(channelRestConnectionFactory);
        final SlackChannel channel = new SlackChannel(new Gson(), auditUtility, restChannelUtility);

        final List<Request> requests = channel.createRequests(event);
        assertFalse(requests.isEmpty(), "Expected requests to be created");
        assertEquals(1, requests.size());
        final Request actualRequest = requests.get(0);
        assertEquals("webhook", actualRequest.getUri());
        assertNotNull(actualRequest.getBodyContent(), "Expected the body content to be set");
    }

    @Test
    public void testCreateRequestMultipleCategoryWithItemUrls() throws Exception {
        final ChannelRestConnectionFactory channelRestConnectionFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_WEBHOOK)).thenReturn(Optional.of("webhook"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_NAME)).thenReturn(Optional.of("slack_channel"));
        Mockito.when(fieldAccessor.getString(SlackDescriptor.KEY_CHANNEL_USERNAME)).thenReturn(Optional.of("user_name"));
        final SortedSet<LinkableItem> items = new TreeSet<>();
        items.add(new LinkableItem("itemName", "itemvalue_1", "itemUrl"));
        items.add(new LinkableItem("itemName", "itemvalue_2", "itemUrl"));
        final CategoryItem categoryItem_1 = new CategoryItem(CategoryKey.from("type", "Key1"), ItemOperation.ADD, 1L, items);
        final CategoryItem categoryItem_2 = new CategoryItem(CategoryKey.from("type", "Key2"), ItemOperation.ADD, 2L, items);

        final SortedSet<CategoryItem> categoryItems = new TreeSet<>();
        categoryItems.add(categoryItem_1);
        categoryItems.add(categoryItem_2);
        final AggregateMessageContent content = new AggregateMessageContent("Message Content", "Slack Unit Test from Alert", categoryItems);
        final DistributionEvent event = Mockito.mock(DistributionEvent.class);
        Mockito.when(event.getFieldAccessor()).thenReturn(fieldAccessor);
        Mockito.when(event.getContent()).thenReturn(MessageContentGroup.singleton(content));

        final RestChannelUtility restChannelUtility = new RestChannelUtility(channelRestConnectionFactory);
        final SlackChannel channel = new SlackChannel(new Gson(), auditUtility, restChannelUtility);

        final List<Request> requests = channel.createRequests(event);
        assertFalse(requests.isEmpty(), "Expected requests to be created");
        assertEquals(1, requests.size());
        final Request actualRequest = requests.get(0);
        assertEquals("webhook", actualRequest.getUri());
        assertNotNull(actualRequest.getBodyContent(), "Expected the body content to be set");
    }

    @Test
    public void testCreateRequestExceptions() {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null);
        List<Request> request = null;

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent messageContent = new AggregateMessageContent("testTopic", "", null, subTopic, new TreeSet<>());

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, "");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, "");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, "ChannelUsername");

        FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        DistributionEvent event = new DistributionEvent(
            "1L", SlackChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        try {
            request = slackChannel.createRequests(event);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request, "Expected the request to be null");
        }

        fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, "Webhook");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, "");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, "ChannelUsername");

        fieldAccessor = new FieldAccessor(fieldModels);
        event = new DistributionEvent(
            "1L", SlackChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        try {
            request = slackChannel.createRequests(event);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request, "Expected the request to be null");
        }
    }

    @Test
    public void testCreateHtmlMessage() throws IntegrationException {
        final RestChannelUtility restChannelUtility = new RestChannelUtility(null);
        final RestChannelUtility restChannelUtilitySpy = Mockito.spy(restChannelUtility);
        Mockito.doNothing().when(restChannelUtilitySpy).sendMessage(Mockito.any(), Mockito.anyString());
        final SlackChannel slackChannel = new SlackChannel(gson, null, restChannelUtilitySpy);
        final AggregateMessageContent messageContent = createMessageContent(getClass().getSimpleName() + ": Request");

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, "Webhook");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, "ChannelName");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, "ChannelUsername");

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        final DistributionEvent event = new DistributionEvent(
            "1L", SlackChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        slackChannel.sendMessage(event);

        //        assertFalse(request.isEmpty());
        Mockito.verify(restChannelUtilitySpy).sendMessage(Mockito.any(), Mockito.anyString());
    }

    @Test
    public void testCreateHtmlMessageEmpty() throws IntegrationException {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null);

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_WEBHOOK, "Webhook");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_NAME, "ChannelName");
        addConfigurationFieldToMap(fieldModels, SlackDescriptor.KEY_CHANNEL_USERNAME, "ChannelUsername");

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        final DistributionEvent event = new DistributionEvent(
            "1L", SlackChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), new MessageContentGroup(), fieldAccessor);
        final SlackChannel spySlackChannel = Mockito.spy(slackChannel);
        final List<Request> requests = slackChannel.createRequests(event);
        assertTrue(requests.isEmpty(), "Expected no requests to be created");
        Mockito.verify(spySlackChannel, Mockito.times(0)).sendMessage(Mockito.any());
    }
}
