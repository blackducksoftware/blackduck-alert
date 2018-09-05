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
package com.synopsys.integration.alert.channel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.assertj.core.util.Sets;
import org.junit.After;
import org.junit.Before;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.digest.model.CategoryData;
import com.synopsys.integration.alert.common.digest.model.ItemData;
import com.synopsys.integration.alert.common.digest.model.ProjectData;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.entity.NotificationCategoryEnum;

public class ChannelTest {
    protected Gson gson;
    protected TestProperties properties;
    protected OutputLogger outputLogger;
    protected ContentConverter contentConverter;

    @Before
    public void init() throws IOException {
        gson = new Gson();
        properties = new TestProperties();
        outputLogger = new OutputLogger();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
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

        final ProjectData projectData = new ProjectData(FrequencyType.REAL_TIME, testName, testName + " Version", Collections.emptyList(), categoryMap);
        return Arrays.asList(projectData);
    }
}
