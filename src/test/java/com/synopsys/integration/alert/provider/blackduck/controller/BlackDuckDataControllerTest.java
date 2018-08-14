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
package com.synopsys.integration.alert.provider.blackduck.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataController;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataHandler;

public class BlackDuckDataControllerTest {

    @Test
    public void getGroupsTest() {
        final BlackDuckDataHandler handler = Mockito.mock(BlackDuckDataHandler.class);
        final BlackDuckDataController controller = new BlackDuckDataController(handler);

        Mockito.when(handler.getBlackDuckGroups()).thenReturn(null);

        final ResponseEntity<String> response = controller.getGroups();
        assertEquals(null, response);
    }

    @Test
    public void getProjectsTest() {
        final BlackDuckDataHandler handler = Mockito.mock(BlackDuckDataHandler.class);
        final BlackDuckDataController controller = new BlackDuckDataController(handler);

        Mockito.when(handler.getBlackDuckProjects()).thenReturn(null);

        final ResponseEntity<String> response = controller.getProjects();
        assertEquals(null, response);
    }

}
