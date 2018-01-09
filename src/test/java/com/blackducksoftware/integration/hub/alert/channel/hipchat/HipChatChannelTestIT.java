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
package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.blackducksoftware.integration.DatabaseSetupRequiredTest;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTest;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;

@Category(DatabaseSetupRequiredTest.class)
public class HipChatChannelTestIT extends ChannelTest {
    @Test
    public void sendMessageTestIT() throws IOException {
        final AuditEntryRepositoryWrapper auditEntryRepository = Mockito.mock(AuditEntryRepositoryWrapper.class);
        final GlobalHubRepositoryWrapper mockedGlobalRepository = Mockito.mock(GlobalHubRepositoryWrapper.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository, null);
        HipChatChannel hipChatChannel = new HipChatChannel(gson, auditEntryRepository, globalProperties, null, null, null);

        final ProjectData data = createProjectData("Integration test project");
        final HipChatEvent event = new HipChatEvent(data, null);
        final int roomId = Integer.parseInt(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));
        final boolean notify = false;
        final String color = "random";
        final HipChatDistributionConfigEntity config = new HipChatDistributionConfigEntity(roomId, notify, color);

        hipChatChannel = Mockito.spy(hipChatChannel);
        Mockito.doReturn(new GlobalHipChatConfigEntity(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY))).when(hipChatChannel).getGlobalConfigEntity();

        hipChatChannel.sendMessage(event, config);

        final String responseLine = getLineContainingText("Successfully sent a HipChat message!");

        assertTrue(!responseLine.isEmpty());
    }

}
