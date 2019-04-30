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
package com.synopsys.integration.alert.channel.hipchat;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.rest.RestChannelUtility;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
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

public class HipChatChannelTest extends ChannelTest {

    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    @Disabled("Hip Chat public api is currently end of life; need an on premise installation to test")
    public void sendMessageTestIT() throws IOException, IntegrationException {
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties, proxyManager);
        final RestChannelUtility restChannelUtility = new RestChannelUtility(channelRestConnectionFactory);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, testAlertProperties, auditUtility, restChannelUtility);

        final AggregateMessageContent messageContent = createMessageContent(getClass().getSimpleName());
        final Boolean notify = false;
        final String color = "random";

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_API_KEY, properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY));
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_COLOR, color);
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_NOTIFY, notify.toString());
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_ROOM_ID, properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);

        final DistributionEvent event = new DistributionEvent("1L", HipChatChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), messageContent, fieldAccessor);

        hipChatChannel.sendAuditedMessage(event);

        final boolean responseLine = outputLogger.isLineContainingText("Successfully sent a " + HipChatChannel.COMPONENT_NAME + " message!");

        assertTrue(responseLine);
    }

    @Test
    public void createRequestThrowsExceptionWhenRoomIdIsNullTest() {
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null);
        IntegrationException intException = null;
        try {
            final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
            final AggregateMessageContent messageContent = new AggregateMessageContent("testTopic", "", null, subTopic, new TreeSet<>());

            final FieldAccessor fieldAccessor = new FieldAccessor(new HashMap<>());
            final DistributionEvent event = new DistributionEvent("1L", HipChatChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), messageContent, fieldAccessor);
            hipChatChannel.createRequests(event);
        } catch (final IntegrationException e) {
            intException = e;
        }
        assertNotNull(intException);
    }

    @Test
    public void createRequestThrowsExceptionForTemplateTest() throws Exception {
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, auditUtility, null);

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent messageContent = new AggregateMessageContent("testTopic", "", null, subTopic, new TreeSet<>());

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_NOTIFY, "false");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_ROOM_ID, "12345");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_API_KEY, "bogusAPIKey");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_HOST_SERVER, "bogusHostServer");

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        final DistributionEvent event = new DistributionEvent("1L", HipChatChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), messageContent, fieldAccessor);

        final String userDir = System.getProperties().getProperty("user.dir");
        try {
            System.getProperties().setProperty("user.dir", "garbage");
            RuntimeException thrownException = null;
            try {
                hipChatChannel.createRequests(event);
            } catch (final RuntimeException e) {
                thrownException = e;
            }
            assertNotNull(thrownException);
        } finally {
            System.getProperties().setProperty("user.dir", userDir);
        }
    }

    @Test
    public void testEmptyContent() throws Exception {
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final TestAlertProperties alertProperties = new TestAlertProperties();
        final RestChannelUtility restChannelUtility = new RestChannelUtility(null);
        final RestChannelUtility restChannelUtilitySpy = Mockito.spy(restChannelUtility);
        Mockito.doNothing().when(restChannelUtilitySpy).sendMessageRequest(Mockito.any(), Mockito.any(), Mockito.anyString());
        Mockito.doReturn(null).when(restChannelUtilitySpy).getIntHttpClient();
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, alertProperties, auditUtility, restChannelUtilitySpy);

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent messageContent = new AggregateMessageContent("testTopic", "", null, subTopic, new TreeSet<>());

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_NOTIFY, "false");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_COLOR, "random");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_ROOM_ID, "12345");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_API_KEY, "bogusAPIKey");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_HOST_SERVER, "bogusHostServer");

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        final DistributionEvent event = new DistributionEvent("1L", HipChatChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), messageContent, fieldAccessor);

        hipChatChannel.sendMessage(event);
        Mockito.verify(restChannelUtilitySpy).sendMessageRequest(Mockito.any(), Mockito.any(), Mockito.anyString());
    }

    @Test
    public void testChunkedRequestList() throws Exception {
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);
        final TestAlertProperties alertProperties = new TestAlertProperties();
        final RestChannelUtility restChannelUtility = new RestChannelUtility(null);
        final RestChannelUtility restChannelUtilitySpy = Mockito.spy(restChannelUtility);
        Mockito.doNothing().when(restChannelUtilitySpy).sendMessageRequest(Mockito.any(), Mockito.any(), Mockito.anyString());
        Mockito.doReturn(null).when(restChannelUtilitySpy).getIntHttpClient();
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, alertProperties, auditUtility, restChannelUtilitySpy);

        final AggregateMessageContent messageContent = createLargeMessageContent();

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_NOTIFY, "false");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_COLOR, "random");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_ROOM_ID, "12345");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_API_KEY, "bogusAPIKey");
        addConfigurationFieldToMap(fieldModels, HipChatDescriptor.KEY_HOST_SERVER, "bogusHostServer");

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        final DistributionEvent event = new DistributionEvent("1L", HipChatChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), messageContent, fieldAccessor);

        hipChatChannel.sendMessage(event);
        Mockito.verify(restChannelUtilitySpy, Mockito.times(3)).sendMessageRequest(Mockito.any(), Mockito.any(), Mockito.anyString());
    }

    private AggregateMessageContent createLargeMessageContent() {
        final AggregateMessageContent messageContent = createMessageContent(getClass().getSimpleName() + ": Chunked Request");
        int count = 0;
        while (gson.toJson(messageContent).length() < HipChatChannel.MESSAGE_SIZE_LIMIT * 3) {
            final LinkableItem newItem = new LinkableItem("Name " + count++, "Relatively long value #" + count + " with some trailing text for good measure...", "https://google.com");
            messageContent.getCategoryItems().iterator().next().getItems().add(newItem);
        }
        return messageContent;
    }
}
