/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.hub.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

public class HubDataControllerTest {

    @Test
    public void getGroupsTest() {
        final HubDataHandler handler = Mockito.mock(HubDataHandler.class);
        final HubDataController controller = new HubDataController(handler);

        Mockito.when(handler.getHubGroups()).thenReturn(null);

        final ResponseEntity<String> response = controller.getGroups();
        assertEquals(null, response);
    }

    @Test
    public void getProjectsTest() {
        final HubDataHandler handler = Mockito.mock(HubDataHandler.class);
        final HubDataController controller = new HubDataController(handler);

        Mockito.when(handler.getHubProjects()).thenReturn(null);

        final ResponseEntity<String> response = controller.getProjects();
        assertEquals(null, response);
    }

}
