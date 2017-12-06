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
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.channel.RestChannelTest;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class HipChatChannelTestIT extends RestChannelTest {

    @Test
    public void sendMessageTestIT() throws IOException {
        Assume.assumeTrue(properties.containsKey("hipchat.api.key"));
        Assume.assumeTrue(properties.containsKey("hipchat.room.id"));

        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        HipChatChannel hipChatChannel = new HipChatChannel(gson, globalProperties, null, null, null);

        final HashMap<NotificationCategoryEnum, CategoryData> categoryMap = new HashMap<>();
        categoryMap.put(NotificationCategoryEnum.POLICY_VIOLATION, createMockPolicyViolation());
        categoryMap.put(NotificationCategoryEnum.MEDIUM_VULNERABILITY, createMockVulnerability());

        final ProjectData data = new ProjectData(DigestTypeEnum.REAL_TIME, "Integration Test Project Name", "Integration Test Project Version Name", categoryMap);
        final HipChatEvent event = new HipChatEvent(data, null);
        final HipChatDistributionConfigEntity config = new HipChatDistributionConfigEntity(Integer.parseInt(properties.getProperty("hipchat.room.id")), false, "random");

        hipChatChannel = Mockito.spy(hipChatChannel);
        Mockito.doReturn(new GlobalHipChatConfigEntity(properties.getProperty("hipchat.api.key"), null, null, null)).when(hipChatChannel).getGlobalConfigEntity();

        hipChatChannel.sendMessage(event, config);

        final String responseLine = getLineContainingText("Successfully sent a HipChat message!");

        assertTrue(!responseLine.isEmpty());
    }

    private CategoryData createMockPolicyViolation() {
        final HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("COMPONENT", "comp");
        dataMap.put("VERSION", "version in violation");
        dataMap.put("RULE", "my policy rule");

        return new CategoryData("POLICY_VIOLATION", Arrays.asList(new ItemData(dataMap)), 1);
    }

    private CategoryData createMockVulnerability() {
        final HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("COMPONENT", "vuln comp");
        dataMap.put("VERSION", "vuln ver");
        dataMap.put("COUNT", 7);

        return new CategoryData("MEDIUM_VULNERABILITY", Arrays.asList(new ItemData(dataMap)), 1);
    }

}
