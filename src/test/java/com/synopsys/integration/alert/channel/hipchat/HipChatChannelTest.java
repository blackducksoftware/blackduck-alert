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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.common.digest.model.DigestModel;
import com.synopsys.integration.alert.common.digest.model.ProjectData;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.connection.UnauthenticatedRestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.test.annotation.ExternalConnectionTest;

public class HipChatChannelTest extends ChannelTest {
    final MockHipChatGlobalEntity hipChatMockUtil = new MockHipChatGlobalEntity();

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(mockedGlobalRepository, new TestAlertProperties());
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        HipChatChannel hipChatChannel = new HipChatChannel(gson, testAlertProperties, globalProperties, auditEntryRepository, null, null, null, channelRestConnectionFactory);

        final Collection<ProjectData> data = createProjectData("Integration test project");
        final DigestModel digestModel = new DigestModel(data);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent event = new ChannelEvent(HipChatChannel.COMPONENT_NAME, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), null, null);
        final int roomId = Integer.parseInt(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));
        final boolean notify = false;
        final String color = "random";
        final HipChatDistributionConfigEntity config = new HipChatDistributionConfigEntity(roomId, notify, color);

        hipChatChannel = Mockito.spy(hipChatChannel);
        Mockito.doReturn(new HipChatGlobalConfigEntity(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY), "")).when(hipChatChannel).getGlobalConfigEntity();

        hipChatChannel.sendAuditedMessage(event, config);

        final boolean responseLine = outputLogger.isLineContainingText("Successfully sent a " + HipChatChannel.COMPONENT_NAME + " message!");

        assertTrue(responseLine);
    }

    public void createRequestThrowsExceptionWhenRoomIdIsNullTest() {
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, null, null);

        IntegrationException intException = null;
        try {
            hipChatChannel.createRequests(new HipChatDistributionConfigEntity(null, null, null), null, null);
        } catch (final IntegrationException e) {
            intException = e;
        }
        assertNotNull(intException);
    }

    @Test
    public void createRequestThrowsExceptionTest() throws Exception {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, auditEntryRepository, null, null, null, null);

        final HipChatDistributionConfigEntity config = new HipChatDistributionConfigEntity(12345, Boolean.FALSE, null);
        final Collection<ProjectData> data = createProjectData("Integration test project");
        final DigestModel digestModel = new DigestModel(data);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent event = new ChannelEvent(HipChatChannel.COMPONENT_NAME, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), null, null);
        final String userDir = System.getProperties().getProperty("user.dir");
        try {
            System.getProperties().setProperty("user.dir", "garbage");
            RuntimeException thrownException = null;
            try {
                hipChatChannel.createRequests(config, hipChatMockUtil.createGlobalEntity(), event);
            } catch (final RuntimeException e) {
                thrownException = e;
            }
            assertNotNull(thrownException);
        } finally {
            System.getProperties().setProperty("user.dir", userDir);
        }
    }

    @Test
    public void testGlobalConfigNullTest() throws Exception {
        final ChannelRestConnectionFactory restFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, null, restFactory);

        Mockito.when(restFactory.createUnauthenticatedRestConnection(Mockito.anyString())).thenReturn(null);

        final String nullEntityMessage = hipChatChannel.testGlobalConfig(null);
        assertEquals("The provided entity was null.", nullEntityMessage);

        hipChatMockUtil.setApiKey("apiKey");
        final HipChatGlobalConfigEntity entityWithKey = hipChatMockUtil.createGlobalEntity();

        final String restConnectionNullMessage = hipChatChannel.testGlobalConfig(entityWithKey);
        assertEquals("Connection error: see logs for more information.", restConnectionNullMessage);
    }

    @Test
    public void testGlobalConfigAPIKeyNullTest() {
        final ChannelRestConnectionFactory restFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, null, restFactory);

        Mockito.when(restFactory.createUnauthenticatedRestConnection(Mockito.anyString())).thenReturn(null);

        try {
            final HipChatGlobalConfigEntity entity = hipChatMockUtil.createEmptyGlobalEntity();
            hipChatChannel.testGlobalConfig(entity);
            fail();
        } catch (final IntegrationException ex) {
            assertEquals("Invalid API key: API key not provided", ex.getMessage());
        }
    }

    @Test
    public void testGlobalConfigValidApiKeyTest() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertTrustCertificate(true);

        final ChannelRestConnectionFactory restFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, null, restFactory);

        hipChatMockUtil.setApiKey(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY));
        hipChatMockUtil.setHostServer("");
        final HipChatGlobalConfigEntity entity = hipChatMockUtil.createGlobalEntity();
        final String validMessage = hipChatChannel.testGlobalConfig(entity);
        assertEquals("API key is valid.", validMessage);
    }

    @Test
    public void testGlobalConfigInvalidApiKeyTest() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertTrustCertificate(true);

        final ChannelRestConnectionFactory restFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, null, restFactory);

        hipChatMockUtil.setApiKey("garbage");
        try {
            final HipChatGlobalConfigEntity entity = hipChatMockUtil.createGlobalEntity();
            hipChatChannel.testGlobalConfig(entity);
        } catch (final IntegrationException ex) {
            assertTrue(ex.getMessage().contains("Invalid API key: "));
        }
    }

    @Test
    public void testGlobalConfigThrowsExceptionTest() throws IntegrationException, MalformedURLException {
        final ChannelRestConnectionFactory restFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, null, restFactory);

        try (final RestConnection restConnection = new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), new URL("http://google.com"), 100, null);) {
            final RestConnection mockRestConnection = Mockito.spy(restConnection);
            Mockito.doThrow(new IntegrationException("Mock exception")).when(mockRestConnection).connect();
            Mockito.when(restFactory.createUnauthenticatedRestConnection(Mockito.anyString())).thenReturn(mockRestConnection);

            hipChatMockUtil.setApiKey("apiKey");
            try {
                final HipChatGlobalConfigEntity entity = hipChatMockUtil.createGlobalEntity();
                hipChatChannel.testGlobalConfig(entity);
            } catch (final IntegrationException ex) {
                assertEquals("Invalid API key: Mock exception", ex.getMessage());
            }
        } catch (final IOException e) {
            Assert.fail();
            e.printStackTrace();
        }

    }

    @Test
    public void testEmptyContent() throws Exception {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final TestAlertProperties alertProperties = new TestAlertProperties();
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(mockedGlobalRepository, alertProperties);
        HipChatChannel hipChatChannel = new HipChatChannel(gson, alertProperties, globalProperties, auditEntryRepository, null, null, null, null);

        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", "");
        final ChannelEvent event = new ChannelEvent(HipChatChannel.COMPONENT_NAME, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), null, null);
        final int roomId = Integer.parseInt(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));
        final boolean notify = false;
        final String color = "random";
        final HipChatDistributionConfigEntity config = new HipChatDistributionConfigEntity(roomId, notify, color);

        hipChatChannel = Mockito.spy(hipChatChannel);
        Mockito.doReturn(new HipChatGlobalConfigEntity(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY), "")).when(hipChatChannel).getGlobalConfigEntity();

        final List<Request> requestList = hipChatChannel.createRequests(config, hipChatChannel.getGlobalConfigEntity(), event);
        assertTrue(requestList.size() == 1);
    }

    @Test
    public void testChunkedRequestList() throws Exception {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final TestAlertProperties alertProperties = new TestAlertProperties();
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(mockedGlobalRepository, alertProperties);
        HipChatChannel hipChatChannel = new HipChatChannel(gson, alertProperties, globalProperties, auditEntryRepository, null, null, null, null);

        final StringBuilder contentBuilder = new StringBuilder(HipChatChannel.MESSAGE_SIZE_LIMIT * 3);
        addContentData(contentBuilder, 'a');
        addContentData(contentBuilder, 'b');
        addContentData(contentBuilder, 'c');

        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentBuilder.toString());
        final ChannelEvent event = new ChannelEvent(HipChatChannel.COMPONENT_NAME, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), null, null);
        final int roomId = Integer.parseInt(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));
        final boolean notify = false;
        final String color = "random";
        final HipChatDistributionConfigEntity config = new HipChatDistributionConfigEntity(roomId, notify, color);

        hipChatChannel = Mockito.spy(hipChatChannel);
        Mockito.doReturn(new HipChatGlobalConfigEntity(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY), "")).when(hipChatChannel).getGlobalConfigEntity();

        final List<Request> requestList = hipChatChannel.createRequests(config, hipChatChannel.getGlobalConfigEntity(), event);
        assertTrue(requestList.size() == 3);
    }

    private void addContentData(final StringBuilder contentBuilder, final char character) {
        for (int index = 0; index < HipChatChannel.MESSAGE_SIZE_LIMIT; index++) {
            contentBuilder.append(character);
        }
    }
}
