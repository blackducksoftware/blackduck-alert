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
package com.blackducksoftware.integration.alert.channel.hipchat;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.TestGlobalProperties;
import com.blackducksoftware.integration.alert.TestPropertyKey;
import com.blackducksoftware.integration.alert.channel.ChannelTest;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRequestHelper;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubRepository;
import com.blackducksoftware.integration.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.connection.UnauthenticatedRestConnection;
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;

public class HipChatChannelTest extends ChannelTest {
    final MockHipChatGlobalEntity hipChatMockUtil = new MockHipChatGlobalEntity();

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(alertEnvironment, mockedGlobalRepository, null);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(globalProperties);
        HipChatChannel hipChatChannel = new HipChatChannel(gson, globalProperties, auditEntryRepository, null, null, null, channelRestConnectionFactory, contentConverter);

        final Collection<ProjectData> data = createProjectData("Integration test project");
        final DigestModel digestModel = new DigestModel(data);
        final ChannelEvent event = new ChannelEvent(HipChatChannel.COMPONENT_NAME, contentConverter.convertToString(digestModel), null);
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
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, null, contentConverter);

        IntegrationException intException = null;
        try {
            hipChatChannel.createRequest(null, new HipChatDistributionConfigEntity(null, null, null), null, null);
        } catch (final IntegrationException e) {
            intException = e;
        }
        assertNotNull(intException);
    }

    @Test
    public void createRequestThrowsExceptionTest() throws Exception {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, auditEntryRepository, null, null, null, null, contentConverter);

        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(null);
        final HipChatDistributionConfigEntity config = new HipChatDistributionConfigEntity(12345, Boolean.FALSE, null);
        final Collection<ProjectData> projectData = createProjectData("HipChat IT test");
        final DigestModel digestModel = new DigestModel(projectData);
        final String userDir = System.getProperties().getProperty("user.dir");
        try {
            System.getProperties().setProperty("user.dir", "garbage");
            RuntimeException thrownException = null;
            try {
                hipChatChannel.createRequest(channelRequestHelper, config, hipChatMockUtil.createGlobalEntity(), digestModel);
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
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, restFactory, contentConverter);

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
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, restFactory, contentConverter);

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
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        globalProperties.setHubTrustCertificate(Boolean.TRUE);

        final ChannelRestConnectionFactory restFactory = new ChannelRestConnectionFactory(globalProperties);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, restFactory, contentConverter);

        hipChatMockUtil.setApiKey(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY));
        hipChatMockUtil.setHostServer("");
        final HipChatGlobalConfigEntity entity = hipChatMockUtil.createGlobalEntity();
        final String validMessage = hipChatChannel.testGlobalConfig(entity);
        assertEquals("API key is valid.", validMessage);
    }

    @Test
    public void testGlobalConfigInvalidApiKeyTest() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        globalProperties.setHubTrustCertificate(Boolean.TRUE);

        final ChannelRestConnectionFactory restFactory = new ChannelRestConnectionFactory(globalProperties);
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, restFactory, contentConverter);

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
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null, null, null, null, restFactory, contentConverter);

        try (RestConnection restConnection = new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), new URL("http://google.com"), 100, null);) {
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

}
