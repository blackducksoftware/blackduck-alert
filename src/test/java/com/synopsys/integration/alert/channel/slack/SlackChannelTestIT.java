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

import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.common.digest.model.CategoryData;
import com.synopsys.integration.alert.common.digest.model.DigestModel;
import com.synopsys.integration.alert.common.digest.model.ItemData;
import com.synopsys.integration.alert.common.digest.model.ProjectData;
import com.synopsys.integration.alert.common.digest.model.ProjectDataFactory;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.entity.NotificationCategoryEnum;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.test.annotation.ExternalConnectionTest;

public class SlackChannelTestIT extends ChannelTest {

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(mockedGlobalRepository, testAlertProperties, null);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final SlackChannel slackChannel = new SlackChannel(gson, testAlertProperties, globalProperties, auditEntryRepository, channelRestConnectionFactory);
        final String roomName = properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME);
        final String username = properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME);
        final String webHook = properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK);

        final Collection<ProjectData> projectData = createProjectData("Slack test project");
        final DigestModel digestModel = new DigestModel(projectData);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final SlackChannelEvent event = new SlackChannelEvent(RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
            notificationContent.getContent(), new Long(0), 1L, webHook, username, roomName);

        slackChannel.sendAuditedMessage(event);

        final boolean actual = outputLogger.isLineContainingText("Successfully sent a " + SlackChannel.COMPONENT_NAME + " message!");
        assertTrue(actual);
    }

    @Test
    public void testCreateRequestExceptions() {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null);
        List<Request> request = null;

        SlackChannelEvent event = new SlackChannelEvent(null, null, null,
            null, null, null, "ChannelUsername", "", "");
        try {
            request = slackChannel.createRequests(null, event);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request);
        }

        event = new SlackChannelEvent(null, null, null,
            null, null, null, "ChannelUsername", "Webhook", "");
        try {
            request = slackChannel.createRequests(null, event);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request);
        }
    }

    @Test
    public void testCreateHtmlMessage() throws IntegrationException {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null);
        final Collection<ProjectData> projectData = createSlackProjectData();
        final DigestModel digestModel = new DigestModel(projectData);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));

        final SlackChannelEvent event = new SlackChannelEvent(RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
            notificationContent.getContent(), new Long(0), 1L, "ChannelUsername", "Webhook", "ChannelName");

        final SlackChannel spySlackChannel = Mockito.spy(slackChannel);
        final List<Request> request = spySlackChannel.createRequests(null, event);

        assertFalse(request.isEmpty());
        Mockito.verify(spySlackChannel).createPostMessageRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
    }

    @Test
    public void testCreateHtmlMessageEmpty() throws IntegrationException {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", "");

        final SlackChannelEvent event = new SlackChannelEvent(RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
            notificationContent.getContent(), new Long(0), 1L, "ChannelUsername", "Webhook", "ChannelName");

        final SlackChannel spySlackChannel = Mockito.spy(slackChannel);
        final List<Request> requests = slackChannel.createRequests(null, event);
        assertTrue(requests.isEmpty());
        Mockito.verify(spySlackChannel, Mockito.times(0)).createPostMessageRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
    }

    private Collection<ProjectData> createSlackProjectData() {
        final Map<NotificationCategoryEnum, CategoryData> categoryMap = new HashMap<>();
        categoryMap.put(NotificationCategoryEnum.HIGH_VULNERABILITY, createCategoryData());

        final ProjectData projectData = new ProjectData(FrequencyType.DAILY, "Slack", "1", null, categoryMap);

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
