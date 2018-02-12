/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.channel.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;

public class EmailPropertiesTest {
    @Test
    public void updateFromConfigTest() {
        final String mailSmtpUser = "guy";
        final String mailSmtpPassword = "xxxxx";
        final Integer mailSmtpPort = 99999;
        final Boolean mailSmtpSendPartial = Boolean.TRUE;
        final GlobalEmailConfigEntity emailConfigEntity = new GlobalEmailConfigEntity(null, mailSmtpUser, mailSmtpPassword, mailSmtpPort, null, null, null, null, null,
                null, null, null, null, mailSmtpSendPartial);

        final EmailProperties emailProperties = new EmailProperties(emailConfigEntity);
        emailProperties.updateFromConfig(emailConfigEntity);

        assertEquals(mailSmtpUser, emailProperties.getJavamailOption(EmailProperties.JAVAMAIL_USER_KEY));
        assertEquals(mailSmtpPort.toString(), emailProperties.getJavamailOption(EmailProperties.JAVAMAIL_PORT_KEY));
        assertEquals(mailSmtpSendPartial.toString(), emailProperties.getJavamailOption(EmailProperties.JAVAMAIL_SEND_PARTIAL_KEY));
        assertEquals(mailSmtpPassword, emailProperties.getMailSmtpPassword());
        assertNotNull(emailProperties.getJavamailConfigProperties());

        IllegalArgumentException caughtException = null;
        try {
            new EmailProperties(null);
        } catch (final IllegalArgumentException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
    }

}
