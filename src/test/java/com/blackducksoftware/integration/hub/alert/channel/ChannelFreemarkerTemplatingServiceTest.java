package com.blackducksoftware.integration.hub.alert.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.AlertConstants;
import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.TestProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;

import freemarker.template.TemplateException;

public class ChannelFreemarkerTemplatingServiceTest {
    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    // TODO find a way to throw an exception to test
    // @Test
    public void testDirectoryFileException() throws IOException {
        System.setProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME, "noooone");
        try {
            @SuppressWarnings("unused")
            final ChannelFreemarkerTemplatingService channelFreemarkerTemplatingService = new ChannelFreemarkerTemplatingService(null);
            fail();
        } catch (final IOException e) {
        }

        assertTrue(outputLogger.isLineContainingText("Error finding the template directory"));
    }

    @Test
    public void testSubjectLine() throws IOException, TemplateException {
        final TestProperties testProperties = new TestProperties();
        final ChannelFreemarkerTemplatingService channelFreemarkerTemplatingService = new ChannelFreemarkerTemplatingService(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_TEMPLATE));
        final String subjectLine = channelFreemarkerTemplatingService.getResolvedSubjectLine(new HashMap<>());

        assertEquals("Default Subject Line - please define one", subjectLine);
    }
}
