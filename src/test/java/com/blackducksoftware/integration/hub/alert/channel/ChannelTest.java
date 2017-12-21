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
package com.blackducksoftware.integration.hub.alert.channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;

import com.blackducksoftware.integration.hub.alert.TestProperties;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.Gson;

public class ChannelTest {
    protected Gson gson;
    protected TestProperties properties;

    private OutputStream systemOut;
    private OutputStream systemErr;
    private OutputStream loggerOutput;

    @Before
    public void init() throws IOException {
        systemOut = System.out;
        systemErr = System.err;
        loggerOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(loggerOutput));
        System.setErr(new PrintStream(loggerOutput));

        gson = new Gson();
        properties = new TestProperties();
    }

    @After
    public void cleanup() throws IOException {
        loggerOutput.close();
        System.setOut(new PrintStream(systemOut));
        System.setErr(new PrintStream(systemErr));
        printLoggerOutput();
    }

    public String getLineContainingText(final String text) throws IOException {
        loggerOutput.flush();
        final String[] consoleLines = loggerOutput.toString().split("\n");

        String lineContainingText = "";
        for (final String line : consoleLines) {
            if (line.contains(text)) {
                lineContainingText = line;
                break;
            }
        }
        return lineContainingText;
    }

    protected CategoryData createMockPolicyViolation() {
        final HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("COMPONENT", "comp");
        dataMap.put("VERSION", "version in violation");
        dataMap.put("RULE", "my policy rule");

        return new CategoryData("POLICY_VIOLATION", Arrays.asList(new ItemData(dataMap)), 1);
    }

    protected CategoryData createMockVulnerability() {
        final HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("COMPONENT", "vuln comp");
        dataMap.put("VERSION", "vuln ver");
        dataMap.put("COUNT", 7);

        return new CategoryData("MEDIUM_VULNERABILITY", Arrays.asList(new ItemData(dataMap)), 1);
    }

    public ProjectData createProjectData(final String testName) {
        final HashMap<NotificationCategoryEnum, CategoryData> categoryMap = new HashMap<>();
        categoryMap.put(NotificationCategoryEnum.POLICY_VIOLATION, createMockPolicyViolation());
        categoryMap.put(NotificationCategoryEnum.MEDIUM_VULNERABILITY, createMockVulnerability());

        final ProjectData projectData = new ProjectData(DigestTypeEnum.REAL_TIME, testName, testName + " Version", categoryMap);
        return projectData;
    }

    // For ease of debugging
    private void printLoggerOutput() {
        try {
            loggerOutput.flush();
        } catch (final IOException e) {
            // Irrelevant to the test
        }
        final String[] consoleLines = loggerOutput.toString().split("\n");
        for (final String line : consoleLines) {
            System.out.println(line);
        }
    }
}
