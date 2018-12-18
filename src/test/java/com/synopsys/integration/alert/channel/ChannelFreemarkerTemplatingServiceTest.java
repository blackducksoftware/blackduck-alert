package com.synopsys.integration.alert.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import com.synopsys.integration.alert.AlertConstants;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;

import freemarker.template.TemplateException;

public class ChannelFreemarkerTemplatingServiceTest {

    @Test
    public void testDirectoryFileException() throws IOException {
        System.setProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME, "noooone");
        try {
            @SuppressWarnings("unused") final ChannelFreemarkerTemplatingService channelFreemarkerTemplatingService = new ChannelFreemarkerTemplatingService(".\\-abdc~2345-9;2");
            fail();
        } catch (final IOException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSubjectLine() throws IOException, TemplateException {
        final TestProperties testProperties = new TestProperties();
        final ChannelFreemarkerTemplatingService channelFreemarkerTemplatingService = new ChannelFreemarkerTemplatingService(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_EMAIL_TEMPLATE));
        final String subjectLine = channelFreemarkerTemplatingService.getResolvedSubjectLine(new HashMap<>());

        assertEquals("Default Subject Line - please define one", subjectLine);
    }
}
