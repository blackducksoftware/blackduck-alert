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
package com.blackducksoftware.integration.hub.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class HubUsersConfigWrapperTest {

    @Test
    public void emptyModelTest() {
        final HubUsersConfigWrapper configWrapper = new HubUsersConfigWrapper();
        assertEquals(7272140956458105198L, HubUsersConfigWrapper.getSerialversionuid());

        assertNull(configWrapper.getId());
        assertNull(configWrapper.getUsername());

        final String expectedString = "{\"username\":null,\"frequency\":null,\"emailConfigId\":null,\"hipChatConfigId\":null,\"slackConfigId\":null,\"projectVersions\":null,\"id\":null}";
        assertEquals(expectedString, configWrapper.toString());

        final HubUsersConfigWrapper newModel = new HubUsersConfigWrapper();
        assertEquals(configWrapper, newModel);
    }

    @Test
    public void modelTest() {
        final String id = "1";
        final String username = "intTester";
        final String frequency = "DAILY";
        final String emailConfigId = "1";
        final String hipChatConfigId = "1";
        final String slackConfigId = "1";

        final List<ProjectVersionConfigWrapper> projectVersions = new ArrayList<>();
        projectVersions.add(new ProjectVersionConfigWrapper("Project 1", "Version 1"));
        projectVersions.add(new ProjectVersionConfigWrapper("Project 1", "Version 2"));
        projectVersions.add(new ProjectVersionConfigWrapper("Project 2", "Version 1"));

        final HubUsersConfigWrapper configWrapper = new HubUsersConfigWrapper(id, username, frequency, emailConfigId, hipChatConfigId, slackConfigId, projectVersions);

        assertEquals(id, configWrapper.getId());
        assertEquals(username, configWrapper.getUsername());

        final String expectedString = "{\"username\":\"" + username + "\",\"frequency\":\"" + frequency + "\",\"emailConfigId\":\"" + emailConfigId + "\",\"hipChatConfigId\":\"" + hipChatConfigId + "\",\"slackConfigId\":\"" + slackConfigId
                + "\",\"projectVersions\":" + projectVersions + ",\"id\":\"" + id + "\"}";
        assertEquals(expectedString, configWrapper.toString());

        final HubUsersConfigWrapper newModel = new HubUsersConfigWrapper(id, username, frequency, emailConfigId, hipChatConfigId, slackConfigId, projectVersions);
        assertEquals(configWrapper, newModel);
    }
}
