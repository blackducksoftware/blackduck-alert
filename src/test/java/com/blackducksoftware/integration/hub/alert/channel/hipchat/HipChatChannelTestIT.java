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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.ResourceLoader;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.Gson;

// TODO write actual integration tests instead of garbage
public class HipChatChannelTestIT {
    private Gson gson;
    private ResourceLoader resourceLoader;
    private Properties properties;

    private OutputStream systemOut;
    private OutputStream systemErr;
    private OutputStream loggerOutput;

    @Before
    public void init() throws IOException {
        gson = new Gson();
        resourceLoader = new ResourceLoader();
        properties = resourceLoader.loadProperties(ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION);

        systemOut = System.out;
        systemErr = System.err;
        loggerOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(loggerOutput));
        System.setErr(new PrintStream(loggerOutput));
    }

    @After
    public void cleanup() throws IOException {
        loggerOutput.close();
        System.setOut(new PrintStream(systemOut));
        System.setErr(new PrintStream(systemErr));
        printLoggerOutput();
    }

    @Test
    public void sendMessageTestIT() throws IOException {
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, null);

        final HashMap<NotificationCategoryEnum, CategoryData> map = new HashMap<>();
        map.put(NotificationCategoryEnum.POLICY_VIOLATION, new CategoryData("category_key", Collections.emptyList(), 0));

        final ProjectData data = new ProjectData(DigestTypeEnum.REAL_TIME, "Integration Test Project Name", "Integration Test Project Version Name", null);
        final HipChatEvent event = new HipChatEvent(data);
        final HipChatConfigEntity config = new HipChatConfigEntity(properties.getProperty("hipchat.api.key"), Integer.parseInt(properties.getProperty("hipchat.room.id")), false, "random");

        hipChatChannel.sendMessage(event, config);

        final String responseLine = getLineContainingText("Successfully sent a HipChat message!");

        assertTrue(!responseLine.isEmpty());
    }

    private String getLineContainingText(final String text) throws IOException {
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
