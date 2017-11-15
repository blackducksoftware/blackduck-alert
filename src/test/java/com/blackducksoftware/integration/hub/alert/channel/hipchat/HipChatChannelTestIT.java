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
import java.util.Collections;
import java.util.HashMap;

import org.junit.Assume;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.channel.RestChannelTest;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class HipChatChannelTestIT extends RestChannelTest {

    @Test
    public void sendMessageTestIT() throws IOException {
        Assume.assumeTrue(properties.containsKey("hipchat.api.key"));
        Assume.assumeTrue(properties.containsKey("hipchat.room.id"));
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null);

        final HashMap<NotificationCategoryEnum, CategoryData> map = new HashMap<>();
        map.put(NotificationCategoryEnum.POLICY_VIOLATION, new CategoryData("category_key", Collections.emptyList(), 0));

        final ProjectData data = new ProjectData(DigestTypeEnum.REAL_TIME, "Integration Test Project Name", "Integration Test Project Version Name", null);
        final HipChatEvent event = new HipChatEvent(data, null);
        final HipChatConfigEntity config = new HipChatConfigEntity(properties.getProperty("hipchat.api.key"), Integer.parseInt(properties.getProperty("hipchat.room.id")), false, "random");

        hipChatChannel.sendMessage(event, config);

        final String responseLine = getLineContainingText("Successfully sent a HipChat message!");

        assertTrue(!responseLine.isEmpty());
    }

}
