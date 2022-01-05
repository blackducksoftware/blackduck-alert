/*
 * test-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class OutputLogger implements AutoCloseable {
    private OutputStream systemOut;
    private OutputStream systemErr;
    private OutputStream loggerOutput;

    public OutputLogger() throws IOException {
        init();
    }

    public void init() throws IOException {
        systemOut = System.out;
        systemErr = System.err;
        loggerOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(loggerOutput));
        System.setErr(new PrintStream(loggerOutput));
    }

    public void cleanup() throws IOException {
        loggerOutput.close();
        System.setOut(new PrintStream(systemOut));
        System.setErr(new PrintStream(systemErr));
        printLoggerOutput();
    }

    public boolean isLineContainingText(String text) throws IOException {
        loggerOutput.flush();
        String[] consoleLines = loggerOutput.toString().split("\n");

        for (String line : consoleLines) {
            if (line.contains(text)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        cleanup();
    }

    private void printLoggerOutput() throws IOException {
        loggerOutput.flush();
        String[] consoleLines = loggerOutput.toString().split("\n");
        for (String line : consoleLines) {
            System.out.println(line);
        }
    }

}
