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
import java.util.Properties;

import org.junit.After;
import org.junit.Before;

import com.blackducksoftware.integration.hub.alert.ResourceLoader;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.google.gson.Gson;

public class RestChannelTest {
    protected Gson gson;
    private ResourceLoader resourceLoader;
    protected Properties properties;

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
        resourceLoader = new ResourceLoader();
        properties = new Properties();
        try {
            properties = resourceLoader.loadProperties(ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION);
        } catch (final Exception ex) {
            System.out.println("Couldn't load " + ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION + " file!");
        }

        if (properties.isEmpty()) {
            for (final TestPropertyKey key : TestPropertyKey.values()) {
                final String prop = System.getenv(key.toString());
                if (prop != null && !prop.isEmpty()) {
                    properties.setProperty(key.getPropertyKey(), prop);
                }
            }
        }
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
