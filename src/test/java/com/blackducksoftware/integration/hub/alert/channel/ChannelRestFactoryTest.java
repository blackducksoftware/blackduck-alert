package com.blackducksoftware.integration.hub.alert.channel;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;

import okhttp3.Request;

public class ChannelRestFactoryTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void testResponseException() throws IntegrationRestException, IOException {
        final ChannelRestFactory channelRestFactory = new ChannelRestFactory("https:url", new TestGlobalProperties(), logger);
        final Request request = channelRestFactory.createRequest("https:url", "", new HashMap<>());
        channelRestFactory.sendRequest(request);

        assertTrue(outputLogger.isLineContainingText("There was a problem sending request"));
    }
}
