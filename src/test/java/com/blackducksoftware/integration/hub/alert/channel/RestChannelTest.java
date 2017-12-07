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
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;

import com.blackducksoftware.integration.hub.alert.ResourceLoader;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class RestChannelTest {
    protected Gson gson;
    private ResourceLoader resourceLoader;
    protected Properties properties;

    private OutputStream systemOut;
    private OutputStream systemErr;
    private OutputStream loggerOutput;

    @Before
    public void init() throws IOException {
        gson = new Gson();
        resourceLoader = new ResourceLoader();
        properties = new Properties();
        try {
            properties = resourceLoader.loadProperties(ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION);
        } catch (final Exception ex) {
            System.out.println("Couldn't load " + ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION + " file!");
        }

        if (properties.isEmpty()) {
            final String applicationJSON = System.getenv(ResourceLoader.PROPERTIES_ENV_VARIABLE);

            if (StringUtils.isNotBlank(applicationJSON)) {
                final JsonParser jsonParser = new JsonParser();
                final JsonElement jsonElement = jsonParser.parse(applicationJSON);
                for (final Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                    final String key = entry.getKey();
                    final String value = entry.getValue().getAsString();
                    properties.put(key, value);
                }
            }
        }
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
