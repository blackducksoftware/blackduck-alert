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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTest;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;

public class SlackChannelTestIT extends ChannelTest {

    @Test
    public void sendMessageTestIT() throws IOException {
        final GlobalHubRepositoryWrapper mockedGlobalRepository = Mockito.mock(GlobalHubRepositoryWrapper.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository, null);

        final SlackChannel slackChannel = new SlackChannel(gson, null, null, globalProperties);
        final String roomName = properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME);
        final String username = properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME);
        final String webHook = properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK);
        final SlackDistributionConfigEntity config = new SlackDistributionConfigEntity(webHook, username, roomName);

        final ProjectData projectData = createProjectData("Slack test project");
        final SlackEvent event = new SlackEvent(projectData, new Long(0));

        slackChannel.sendMessage(event, config);

        final String actual = getLineContainingText("Successfully sent a slack message!");
        assertTrue(!actual.isEmpty());
    }

}
