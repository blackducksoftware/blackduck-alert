package com.blackducksoftware.integration.alert;

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

    private void printLoggerOutput() throws IOException {
        loggerOutput.flush();
        final String[] consoleLines = loggerOutput.toString().split("\n");
        for (final String line : consoleLines) {
            System.out.println(line);
        }
    }

    public boolean isLineContainingText(final String text) throws IOException {
        loggerOutput.flush();
        final String[] consoleLines = loggerOutput.toString().split("\n");

        for (final String line : consoleLines) {
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
}
