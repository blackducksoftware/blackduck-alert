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
package com.blackducksoftware.integration.hub.alert.channel.slack;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.util.Sets;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTest;
import com.blackducksoftware.integration.hub.alert.channel.rest.ChannelRequestHelper;
import com.blackducksoftware.integration.hub.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;

public class SlackChannelTestIT extends ChannelTest {

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository, null);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(globalProperties);
        final SlackChannel slackChannel = new SlackChannel(gson, auditEntryRepository, null, null, channelRestConnectionFactory, contentConverter);
        final String roomName = properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME);
        final String username = properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME);
        final String webHook = properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK);
        final SlackDistributionConfigEntity config = new SlackDistributionConfigEntity(webHook, username, roomName);

        final Collection<ProjectData> projectData = createProjectData("Slack test project");
        final DigestModel digestModel = new DigestModel(projectData);
        final ChannelEvent event = new ChannelEvent(SlackChannel.COMPONENT_NAME, contentConverter.convertToString(digestModel), new Long(0));

        slackChannel.sendAuditedMessage(event, config);

        final boolean actual = outputLogger.isLineContainingText("Successfully sent a slack_channel message!");
        assertTrue(actual);
    }

    @Test
    public void testCreateRequestExceptions() {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null, contentConverter);
        final MockSlackEntity mockSlackEntity = new MockSlackEntity();
        Request request = null;

        try {
            request = slackChannel.createRequest(null, mockSlackEntity.createEmptyEntity(), null, null);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request);
        }

        mockSlackEntity.setChannelName("");
        try {
            request = slackChannel.createRequest(null, mockSlackEntity.createEntity(), null, null);
            fail();
        } catch (final IntegrationException e) {
            assertNull(request);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateHtmlMessage() throws IntegrationException {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null, contentConverter);
        final MockSlackEntity mockSlackEntity = new MockSlackEntity();
        final Collection<ProjectData> projectData = createSlackProjectData();
        final DigestModel digestModel = new DigestModel(projectData);

        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(null) {
            @Override
            public Request createPostMessageRequest(final String url, final Map<String, String> headers, final String body) {
                assertTrue(body.contains("Vulnerability Count Added: "));
                assertTrue(body.contains("Vulnerability Count Updated: "));
                assertTrue(body.contains("Vulnerability Count Deleted: "));
                return null;
            }
        };

        final ChannelRequestHelper spyChannelRequestHelper = Mockito.spy(channelRequestHelper);

        final Request request = slackChannel.createRequest(spyChannelRequestHelper, mockSlackEntity.createEntity(), null, digestModel);

        assertNull(request);

        Mockito.verify(spyChannelRequestHelper).createPostMessageRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateHtmlMessageEmpty() throws IntegrationException {
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null, contentConverter);
        final MockSlackEntity mockSlackEntity = new MockSlackEntity();
        final ProjectData projectData = new ProjectData(DigestTypeEnum.DAILY, "Slack", "1", null, null);
        final DigestModel digestModel = new DigestModel(Arrays.asList(projectData));
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(null) {
            @Override
            public Request createPostMessageRequest(final String url, final Map<String, String> headers, final String body) {
                assertTrue(body.contains("A notification was received, but it was empty."));
                return null;
            }
        };

        final ChannelRequestHelper spyChannelRequestHelper = Mockito.spy(channelRequestHelper);

        final Request request = slackChannel.createRequest(spyChannelRequestHelper, mockSlackEntity.createEntity(), null, digestModel);

        assertNull(request);

        Mockito.verify(spyChannelRequestHelper).createPostMessageRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
    }

    private Collection<ProjectData> createSlackProjectData() {
        final Map<NotificationCategoryEnum, CategoryData> categoryMap = new HashMap<>();
        categoryMap.put(NotificationCategoryEnum.HIGH_VULNERABILITY, createCategoryData());

        final ProjectData projectData = new ProjectData(DigestTypeEnum.DAILY, "Slack", "1", null, categoryMap);

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
