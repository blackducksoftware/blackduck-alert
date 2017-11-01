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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assume;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.channel.RestChannelTest;
import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class SlackChannelTestIT extends RestChannelTest {

    @Test
    public void sendMessageTestIT() throws IOException {
        Assume.assumeTrue(properties.containsKey("slack.channel.name"));
        Assume.assumeTrue(properties.containsKey("slack.username"));
        Assume.assumeTrue(properties.containsKey("slack.web.hook"));

        final SlackChannel slackChannel = new SlackChannel(gson, null);
        final String roomName = properties.getProperty("slack.channel.name");
        final String username = properties.getProperty("slack.username");
        final String webHook = properties.getProperty("slack.web.hook");
        final SlackConfigEntity config = new SlackConfigEntity(roomName, username, webHook);

        final HashMap<NotificationCategoryEnum, CategoryData> map = new HashMap<>();
        final Map<String, Object> dataSet = new HashMap<>();
        dataSet.put("uh", "what");
        final ItemData itemData = new ItemData(dataSet);
        final ArrayList<ItemData> data = new ArrayList<>();
        data.add(itemData);
        map.put(NotificationCategoryEnum.POLICY_VIOLATION, new CategoryData("test violation", data, 3));

        final ProjectData projectData = new ProjectData(DigestTypeEnum.REAL_TIME, "Test Project", "0.0.1-TEST", map);
        final SlackEvent event = new SlackEvent(projectData, new Long(0));

        slackChannel.sendMessage(event, config);

        final String actual = getLineContainingText("Successfully sent a slack message!");
        assertTrue(!actual.isEmpty());
    }
}
