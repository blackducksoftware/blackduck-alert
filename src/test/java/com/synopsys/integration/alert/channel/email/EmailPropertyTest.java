package com.synopsys.integration.alert.channel.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.synopsys.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.synopsys.integration.alert.web.channel.model.EmailGlobalConfig;

public class EmailPropertyTest {

    @Test
    public void testProperties() {
        final MockEmailGlobalEntity emailGlobalEntity = new MockEmailGlobalEntity();
        final EmailProperties emailProperties = new EmailProperties(emailGlobalEntity.createGlobalEntity());

        assertEquals(emailGlobalEntity.getMailSmtpPassword(), emailProperties.getMailSmtpPassword());
    }

    @Test
    public void testPropertiesException() {
        EmailProperties emailProperties = null;
        try {
            emailProperties = new EmailProperties((EmailGlobalConfig) null);
            fail();
        } catch (final IllegalArgumentException e) {

        } catch (final Exception e) {
            fail();
        }

        assertTrue(emailProperties == null);
    }
}
