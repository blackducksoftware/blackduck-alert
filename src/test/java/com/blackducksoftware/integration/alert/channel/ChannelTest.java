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
package com.blackducksoftware.integration.alert.channel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.assertj.core.util.Sets;
import org.junit.After;
import org.junit.Before;

import com.blackducksoftware.integration.alert.OutputLogger;
import com.blackducksoftware.integration.alert.TestProperties;
import com.blackducksoftware.integration.alert.config.AlertEnvironment;
import com.blackducksoftware.integration.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.alert.digest.model.ItemData;
import com.blackducksoftware.integration.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.alert.event.AlertEventContentConverter;
import com.google.gson.Gson;

public class ChannelTest {
    protected Gson gson;
    protected TestProperties properties;
    protected OutputLogger outputLogger;
    protected AlertEventContentConverter contentConverter;
    protected AlertEnvironment alertEnvironment;

    @Before
    public void init() throws IOException {
        gson = new Gson();
        properties = new TestProperties();
        outputLogger = new OutputLogger();
        contentConverter = new AlertEventContentConverter(gson);
        alertEnvironment = new AlertEnvironment();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    protected CategoryData createMockPolicyViolation() {
        final HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("COMPONENT", "comp");
        dataMap.put("VERSION", "version in violation");
        dataMap.put("RULE", "my policy rule");

        return new CategoryData("POLICY_VIOLATION", Sets.newLinkedHashSet(new ItemData(dataMap)), 1);
    }

    protected CategoryData createMockVulnerability() {
        final HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("COMPONENT", "vuln comp");
        dataMap.put("VERSION", "vuln ver");
        dataMap.put("COUNT", 7);

        return new CategoryData("MEDIUM_VULNERABILITY", Sets.newLinkedHashSet(new ItemData(dataMap)), 1);
    }

    public Collection<ProjectData> createProjectData(final String testName) {
        final HashMap<NotificationCategoryEnum, CategoryData> categoryMap = new HashMap<>();
        categoryMap.put(NotificationCategoryEnum.POLICY_VIOLATION, createMockPolicyViolation());
        categoryMap.put(NotificationCategoryEnum.MEDIUM_VULNERABILITY, createMockVulnerability());

        final ProjectData projectData = new ProjectData(DigestTypeEnum.REAL_TIME, testName, testName + " Version", Collections.emptyList(), categoryMap);
        return Arrays.asList(projectData);
    }
}
