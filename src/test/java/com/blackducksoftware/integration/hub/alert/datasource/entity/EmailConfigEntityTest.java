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
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class EmailConfigEntityTest {

    @Test
    public void testEmptyModel() {
        final EmailConfigEntity emailConfigEntity = new EmailConfigEntity();
        assertEquals(4122029411365267232L, EmailConfigEntity.getSerialversionuid());

        assertNull(emailConfigEntity.getEmailSubjectLine());
        assertNull(emailConfigEntity.getEmailTemplateDirectory());
        assertNull(emailConfigEntity.getEmailTemplateLogoImage());
        assertNull(emailConfigEntity.getId());
        assertNull(emailConfigEntity.getMailSmtpAllow8bitmime());
        assertNull(emailConfigEntity.getMailSmtpAuth());
        assertNull(emailConfigEntity.getMailSmtpConnectionTimeout());
        assertNull(emailConfigEntity.getMailSmtpDnsNotify());
        assertNull(emailConfigEntity.getMailSmtpDnsRet());
        assertNull(emailConfigEntity.getMailSmtpEhlo());
        assertNull(emailConfigEntity.getMailSmtpFrom());
        assertNull(emailConfigEntity.getMailSmtpHost());
        assertNull(emailConfigEntity.getMailSmtpLocalhost());
        assertNull(emailConfigEntity.getMailSmtpPassword());
        assertNull(emailConfigEntity.getMailSmtpPort());
        assertNull(emailConfigEntity.getMailSmtpSendPartial());
        assertNull(emailConfigEntity.getMailSmtpTimeout());
        assertNull(emailConfigEntity.getMailSmtpUser());

        assertEquals(1169169065, emailConfigEntity.hashCode());

        final String expectedString = "{\"mailSmtpHost\":null,\"mailSmtpUser\":null,\"mailSmtpPort\":null,\"mailSmtpConnectionTimeout\":null,\"mailSmtpTimeout\":null,\"mailSmtpFrom\":null,\"mailSmtpLocalhost\":null,\"mailSmtpEhlo\":null,\"mailSmtpAuth\":null,\"mailSmtpDnsNotify\":null,\"mailSmtpDnsRet\":null,\"mailSmtpAllow8bitmime\":null,\"mailSmtpSendPartial\":null,\"emailTemplateDirectory\":null,\"emailTemplateLogoImage\":null,\"emailSubjectLine\":null,\"id\":null}";
        assertEquals(expectedString, emailConfigEntity.toString());

        final EmailConfigEntity emailConfigEntityNew = new EmailConfigEntity();
        assertEquals(emailConfigEntity, emailConfigEntityNew);
    }

    @Test
    public void testModel() {
        final Long id = 123L;
        final String mailSmtpHost = "MailSmtpHost";
        final String mailSmtpUser = "MailSmtpUser";
        final String mailSmtpPassword = "MailSmtpPassword";
        final Integer mailSmtpPort = 111;
        final Integer mailSmtpConnectionTimeout = 222;
        final Integer mailSmtpTimeout = 333;
        final String mailSmtpFrom = "MailSmtpFrom";
        final String mailSmtpLocalhost = "MailSmtpLocalhost";
        final Boolean mailSmtpEhlo = true;
        final Boolean mailSmtpAuth = false;
        final String mailSmtpDnsNotify = "MailSmtpDnsNotify";
        final String mailSmtpDnsRet = "MailSmtpDnsRet";
        final Boolean mailSmtpAllow8bitmime = false;
        final Boolean mailSmtpSendPartial = true;
        final String emailTemplateDirectory = "EmailTemplateDirectory";
        final String emailTemplateLogoImage = "EmailTemplateLogoImage";
        final String emailSubjectLine = "EmailSubjectLine";

        final EmailConfigEntity emailConfigEntity = new EmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo, mailSmtpAuth,
                mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        emailConfigEntity.setId(id);
        assertEquals(emailSubjectLine, emailConfigEntity.getEmailSubjectLine());
        assertEquals(emailTemplateDirectory, emailConfigEntity.getEmailTemplateDirectory());
        assertEquals(emailTemplateLogoImage, emailConfigEntity.getEmailTemplateLogoImage());
        assertEquals(id, emailConfigEntity.getId());
        assertEquals(mailSmtpAllow8bitmime, emailConfigEntity.getMailSmtpAllow8bitmime());
        assertEquals(mailSmtpAuth, emailConfigEntity.getMailSmtpAuth());
        assertEquals(mailSmtpConnectionTimeout, emailConfigEntity.getMailSmtpConnectionTimeout());
        assertEquals(mailSmtpDnsNotify, emailConfigEntity.getMailSmtpDnsNotify());
        assertEquals(mailSmtpDnsRet, emailConfigEntity.getMailSmtpDnsRet());
        assertEquals(mailSmtpEhlo, emailConfigEntity.getMailSmtpEhlo());
        assertEquals(mailSmtpFrom, emailConfigEntity.getMailSmtpFrom());
        assertEquals(mailSmtpHost, emailConfigEntity.getMailSmtpHost());
        assertEquals(mailSmtpLocalhost, emailConfigEntity.getMailSmtpLocalhost());
        assertEquals(mailSmtpPassword, emailConfigEntity.getMailSmtpPassword());
        assertEquals(mailSmtpPort, emailConfigEntity.getMailSmtpPort());
        assertEquals(mailSmtpSendPartial, emailConfigEntity.getMailSmtpSendPartial());
        assertEquals(mailSmtpTimeout, emailConfigEntity.getMailSmtpTimeout());
        assertEquals(mailSmtpUser, emailConfigEntity.getMailSmtpUser());

        assertEquals(1668272119, emailConfigEntity.hashCode());

        final String expectedString = "{\"mailSmtpHost\":\"MailSmtpHost\",\"mailSmtpUser\":\"MailSmtpUser\",\"mailSmtpPort\":111,\"mailSmtpConnectionTimeout\":222,\"mailSmtpTimeout\":333,\"mailSmtpFrom\":\"MailSmtpFrom\",\"mailSmtpLocalhost\":\"MailSmtpLocalhost\",\"mailSmtpEhlo\":true,\"mailSmtpAuth\":false,\"mailSmtpDnsNotify\":\"MailSmtpDnsNotify\",\"mailSmtpDnsRet\":\"MailSmtpDnsRet\",\"mailSmtpAllow8bitmime\":false,\"mailSmtpSendPartial\":true,\"emailTemplateDirectory\":\"EmailTemplateDirectory\",\"emailTemplateLogoImage\":\"EmailTemplateLogoImage\",\"emailSubjectLine\":\"EmailSubjectLine\",\"id\":123}";
        assertEquals(expectedString, emailConfigEntity.toString());

        final EmailConfigEntity emailConfigEntityNew = new EmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo,
                mailSmtpAuth, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        emailConfigEntityNew.setId(id);
        assertEquals(emailConfigEntity, emailConfigEntityNew);
    }

}
