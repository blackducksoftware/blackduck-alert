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
package com.synopsys.integration.alert.common.digest.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.entity.NotificationCategoryEnum;

public class ProjectDataTest {

    @Test
    public void testDataNull() {
        final ProjectData projectData = new ProjectData(null, null, null, null, null);

        assertNull(projectData.getCategoryMap());
        assertNull(projectData.getFrequencyType());
        assertNull(projectData.getProjectKey());
        assertNull(projectData.getProjectName());
        assertNull(projectData.getProjectVersion());

        assertEquals("{\"frequencyType\":null,\"projectKey\":null,\"projectName\":null,\"projectVersion\":null,\"notificationIds\":null,\"categoryMap\":null}", projectData.toString());
    }

    @Test
    public void testProjectKey() {
        ProjectData projectData = new ProjectData(null, null, null, null, null);
        assertNull(projectData.getProjectKey());

        projectData = new ProjectData(null, null, "Version", null, null);
        assertEquals("Version", projectData.getProjectKey());

        projectData = new ProjectData(null, "Project", null, null, null);
        assertEquals("Project", projectData.getProjectKey());

        projectData = new ProjectData(null, "Project", "Version", null, null);
        assertEquals("ProjectVersion", projectData.getProjectKey());
    }

    @Test
    public void testData() {
        final CategoryData categoryData = new CategoryData(null, null, 0);
        final Map<NotificationCategoryEnum, CategoryData> categoryMap = new HashMap<>();
        categoryMap.put(NotificationCategoryEnum.HIGH_VULNERABILITY, categoryData);

        final List<Long> notificationIds = new ArrayList<>();
        notificationIds.add(1L);

        final ProjectData projectData = new ProjectData(FrequencyType.REAL_TIME, "Project", "Version", notificationIds, categoryMap);

        assertEquals(categoryMap, projectData.getCategoryMap());
        assertEquals(FrequencyType.REAL_TIME, projectData.getFrequencyType());
        assertEquals("ProjectVersion", projectData.getProjectKey());
        assertEquals("Project", projectData.getProjectName());
        assertEquals("Version", projectData.getProjectVersion());

        assertEquals(
        "{\"frequencyType\":\"REAL_TIME\",\"projectKey\":\"ProjectVersion\",\"projectName\":\"Project\",\"projectVersion\":\"Version\",\"notificationIds\":[1],\"categoryMap\":{HIGH_VULNERABILITY={\"categoryKey\":null,\"items\":null,\"itemCount\":0}}}",
        projectData.toString());
    }
}
