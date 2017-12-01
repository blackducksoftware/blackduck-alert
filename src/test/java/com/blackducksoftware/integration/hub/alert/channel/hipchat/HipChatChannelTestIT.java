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

import com.blackducksoftware.integration.hub.alert.channel.RestChannelTest;

//FIXME
public class HipChatChannelTestIT extends RestChannelTest {

    // @Test
    // public void sendMessageTestIT() throws IOException {
    // Assume.assumeTrue(properties.containsKey("hipchat.api.key"));
    // Assume.assumeTrue(properties.containsKey("hipchat.room.id"));
    // final HipChatChannel hipChatChannel = new HipChatChannel(gson, null, null);
    //
    // final HashMap<NotificationCategoryEnum, CategoryData> map = new HashMap<>();
    // map.put(NotificationCategoryEnum.POLICY_VIOLATION, new CategoryData("category_key", Collections.emptyList(), 0));
    //
    // final ProjectData data = new ProjectData(DigestTypeEnum.REAL_TIME, "Integration Test Project Name", "Integration Test Project Version Name", null);
    // final HipChatEvent event = new HipChatEvent(data, null);
    // final GlobalHipChatConfigEntity config = new GlobalHipChatConfigEntity(properties.getProperty("hipchat.api.key"), Integer.parseInt(properties.getProperty("hipchat.room.id")), false, "random");
    //
    // hipChatChannel.sendMessage(event, config);
    //
    // final String responseLine = getLineContainingText("Successfully sent a HipChat message!");
    //
    // assertTrue(!responseLine.isEmpty());
    // }

}
