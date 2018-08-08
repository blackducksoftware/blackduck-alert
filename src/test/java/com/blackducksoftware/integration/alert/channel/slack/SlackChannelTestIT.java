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
package com.blackducksoftware.integration.alert.channel.slack;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Sets;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.TestAlertProperties;
import com.blackducksoftware.integration.alert.TestBlackDuckProperties;
import com.blackducksoftware.integration.alert.TestPropertyKey;
import com.blackducksoftware.integration.alert.channel.ChannelTest;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRequestHelper;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.alert.channel.slack.mock.MockSlackEntity;
import com.blackducksoftware.integration.alert.common.digest.model.CategoryData;
import com.blackducksoftware.integration.alert.common.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.common.digest.model.ItemData;
import com.blackducksoftware.integration.alert.common.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.common.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;

public class SlackChannelTestIT extends ChannelTest {

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(mockedGlobalRepository, testAlertProperties, null);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final SlackChannel slackChannel = new SlackChannel(gson, testAlertProperties, globalProperties, auditEntryRepository, null, null, channelRestConnectionFactory, contentConverter);
        final String roomName = properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME);
        final String username = properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME);
        final String webHook = properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK);
        final SlackDistributionConfigEntity config = new SlackDistributionConfigEntity(webHook, username, roomName);

        final Collection<ProjectData> projectData = createProjectData("Slack test project");
        final DigestModel digestModel = new DigestModel(projectData);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent event = new ChannelEvent(SlackChannel.COMPONENT_NAME, RestConnection.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), new Long(0), 1L);

        slackChannel.sendAuditedMessage(event, config);

        final boolean actual = outputLogger.isLineContainingText("Successfully sent a " + SlackChannel.COMPONENT_NAME + " message!");
        assertTrue(actual);
    }

    @Test
    public void testCreateRequestExceptions() {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null, null, null, contentConverter);
        final MockSlackEntity mockSlackEntity = new MockSlackEntity();
        List<Request> request = null;

        try {
            request = slackChannel.createRequests(null, mockSlackEntity.createEmptyEntity(), null, null);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request);
        }

        mockSlackEntity.setChannelName("");
        try {
            request = slackChannel.createRequests(null, mockSlackEntity.createEntity(), null, null);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request);
        }
    }

    @Test
    public void testCreateHtmlMessage() throws IntegrationException {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null, null, null, contentConverter);
        final MockSlackEntity mockSlackEntity = new MockSlackEntity();
        final Collection<ProjectData> projectData = createSlackProjectData();
        final DigestModel digestModel = new DigestModel(projectData);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent event = new ChannelEvent(SlackChannel.COMPONENT_NAME, RestConnection.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), new Long(0), 1L);

        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(null) {
            @Override
            public Request createPostMessageRequest(final String url, final Map<String, String> headers, final String body) {
                assertTrue(body.contains("HIGH_VULNERABILITY"));
                assertTrue(body.contains("ADDED"));
                assertTrue(body.contains("UPDATED"));
                assertTrue(body.contains("DELETED"));
                return null;
            }
        };

        final ChannelRequestHelper spyChannelRequestHelper = Mockito.spy(channelRequestHelper);

        final List<Request> request = slackChannel.createRequests(spyChannelRequestHelper, mockSlackEntity.createEntity(), null, event);

        assertFalse(request.isEmpty());
        Mockito.verify(spyChannelRequestHelper).createPostMessageRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
    }

    @Test
    public void testCreateHtmlMessageEmpty() throws IntegrationException {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null, null, null, contentConverter);
        final MockSlackEntity mockSlackEntity = new MockSlackEntity();
        final ProjectData projectData = new ProjectData(DigestType.DAILY, "Slack", "1", null, null);
        final DigestModel digestModel = new DigestModel(Arrays.asList(projectData));
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", "");
        final ChannelEvent event = new ChannelEvent(SlackChannel.COMPONENT_NAME, RestConnection.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), new Long(0), 1L);

        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(null) {
            @Override
            public Request createPostMessageRequest(final String url, final Map<String, String> headers, final String body) {
                assertTrue(body.contains("A notification was received, but it was empty."));
                return null;
            }
        };

        final ChannelRequestHelper spyChannelRequestHelper = Mockito.spy(channelRequestHelper);

        final List<Request> requests = slackChannel.createRequests(spyChannelRequestHelper, mockSlackEntity.createEntity(), null, event);
        assertFalse(requests.isEmpty());
        Mockito.verify(spyChannelRequestHelper).createPostMessageRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
    }

    private Collection<ProjectData> createSlackProjectData() {
        final Map<NotificationCategoryEnum, CategoryData> categoryMap = new HashMap<>();
        categoryMap.put(NotificationCategoryEnum.HIGH_VULNERABILITY, createCategoryData());

        final ProjectData projectData = new ProjectData(DigestType.DAILY, "Slack", "1", null, categoryMap);

        return Arrays.asList(projectData);
    }

    private CategoryData createCategoryData() {
        final Map<String, Object> itemDataDataSet = new HashMap<>();
        itemDataDataSet.put(ProjectDataFactory.VULNERABILITY_COUNT_KEY_ADDED, 1);
        itemDataDataSet.put(ProjectDataFactory.VULNERABILITY_COUNT_KEY_UPDATED, 1);
        itemDataDataSet.put(ProjectDataFactory.VULNERABILITY_COUNT_KEY_DELETED, 1);

        final CategoryData categoryData = new CategoryData("key", Sets.newLinkedHashSet(new ItemData(itemDataDataSet)), 0);

        return categoryData;
    }

}
