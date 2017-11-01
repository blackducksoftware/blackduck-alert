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
package com.blackducksoftware.integration.hub.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class EmailConfigRestModelTest {

    @Test
    public void testEmptyModel() {
        final EmailConfigRestModel emailConfigRestModel = new EmailConfigRestModel();
        assertEquals(9172607945030111585L, EmailConfigRestModel.getSerialversionuid());

        assertNull(emailConfigRestModel.getEmailSubjectLine());
        assertNull(emailConfigRestModel.getEmailTemplateDirectory());
        assertNull(emailConfigRestModel.getEmailTemplateLogoImage());
        assertNull(emailConfigRestModel.getId());
        assertNull(emailConfigRestModel.getMailSmtpAllow8bitmime());
        assertNull(emailConfigRestModel.getMailSmtpAuth());
        assertNull(emailConfigRestModel.getMailSmtpConnectionTimeout());
        assertNull(emailConfigRestModel.getMailSmtpDnsNotify());
        assertNull(emailConfigRestModel.getMailSmtpDnsRet());
        assertNull(emailConfigRestModel.getMailSmtpEhlo());
        assertNull(emailConfigRestModel.getMailSmtpFrom());
        assertNull(emailConfigRestModel.getMailSmtpHost());
        assertNull(emailConfigRestModel.getMailSmtpLocalhost());
        assertNull(emailConfigRestModel.getMailSmtpPassword());
        assertNull(emailConfigRestModel.getMailSmtpPort());
        assertNull(emailConfigRestModel.getMailSmtpSendPartial());
        assertNull(emailConfigRestModel.getMailSmtpTimeout());
        assertNull(emailConfigRestModel.getMailSmtpUser());

        assertEquals(1169169065, emailConfigRestModel.hashCode());

        final String expectedString = "{\"mailSmtpHost\":null,\"mailSmtpUser\":null,\"mailSmtpPort\":null,\"mailSmtpConnectionTimeout\":null,\"mailSmtpTimeout\":null,\"mailSmtpFrom\":null,\"mailSmtpLocalhost\":null,\"mailSmtpEhlo\":null,\"mailSmtpAuth\":null,\"mailSmtpDnsNotify\":null,\"mailSmtpDnsRet\":null,\"mailSmtpAllow8bitmime\":null,\"mailSmtpSendPartial\":null,\"emailTemplateDirectory\":null,\"emailTemplateLogoImage\":null,\"emailSubjectLine\":null,\"id\":null}";
        assertEquals(expectedString, emailConfigRestModel.toString());

        final EmailConfigRestModel emailConfigRestModelNew = new EmailConfigRestModel();
        assertEquals(emailConfigRestModel, emailConfigRestModelNew);
    }

    @Test
    public void testModel() {
        final String emailSubjectLine = "EmailSubjectLine";
        final String emailTemplateDirectory = "EmailTemplateDirectory";
        final String emailTemplateLogoImage = "EmailTemplateLogoImage";
        final String id = "Id";
        final String mailSmtpAllow8bitmime = "MailSmtpAllow8bitmime";
        final String mailSmtpAuth = "MailSmtpAuth";
        final String mailSmtpConnectionTimeout = "MailSmtpConnectionTimeout";
        final String mailSmtpDnsNotify = "MailSmtpDnsNotify";
        final String mailSmtpDnsRet = "MailSmtpDnsRet";
        final String mailSmtpEhlo = "MailSmtpEhlo";
        final String mailSmtpFrom = "MailSmtpFrom";
        final String mailSmtpHost = "MailSmtpHost";
        final String mailSmtpLocalhost = "MailSmtpLocalhost";
        final String mailSmtpPassword = "MailSmtpPassword";
        final String mailSmtpPort = "mailSmtpPort";
        final String mailSmtpSendPartial = "MailSmtpSendPartial";
        final String mailSmtpTimeout = "MailSmtpTimeout";
        final String mailSmtpUser = "MailSmtpUser";

        final EmailConfigRestModel emailConfigRestModel = new EmailConfigRestModel(id, mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo,
                mailSmtpAuth, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        assertEquals(emailSubjectLine, emailConfigRestModel.getEmailSubjectLine());
        assertEquals(emailTemplateDirectory, emailConfigRestModel.getEmailTemplateDirectory());
        assertEquals(emailTemplateLogoImage, emailConfigRestModel.getEmailTemplateLogoImage());
        assertEquals(id, emailConfigRestModel.getId());
        assertEquals(mailSmtpAllow8bitmime, emailConfigRestModel.getMailSmtpAllow8bitmime());
        assertEquals(mailSmtpAuth, emailConfigRestModel.getMailSmtpAuth());
        assertEquals(mailSmtpConnectionTimeout, emailConfigRestModel.getMailSmtpConnectionTimeout());
        assertEquals(mailSmtpDnsNotify, emailConfigRestModel.getMailSmtpDnsNotify());
        assertEquals(mailSmtpDnsRet, emailConfigRestModel.getMailSmtpDnsRet());
        assertEquals(mailSmtpEhlo, emailConfigRestModel.getMailSmtpEhlo());
        assertEquals(mailSmtpFrom, emailConfigRestModel.getMailSmtpFrom());
        assertEquals(mailSmtpHost, emailConfigRestModel.getMailSmtpHost());
        assertEquals(mailSmtpLocalhost, emailConfigRestModel.getMailSmtpLocalhost());
        assertEquals(mailSmtpPassword, emailConfigRestModel.getMailSmtpPassword());
        assertEquals(mailSmtpPort, emailConfigRestModel.getMailSmtpPort());
        assertEquals(mailSmtpSendPartial, emailConfigRestModel.getMailSmtpSendPartial());
        assertEquals(mailSmtpTimeout, emailConfigRestModel.getMailSmtpTimeout());
        assertEquals(mailSmtpUser, emailConfigRestModel.getMailSmtpUser());

        assertEquals(1376293102, emailConfigRestModel.hashCode());

        final String expectedString = "{\"mailSmtpHost\":\"MailSmtpHost\",\"mailSmtpUser\":\"MailSmtpUser\",\"mailSmtpPort\":\"mailSmtpPort\",\"mailSmtpConnectionTimeout\":\"MailSmtpConnectionTimeout\",\"mailSmtpTimeout\":\"MailSmtpTimeout\",\"mailSmtpFrom\":\"MailSmtpFrom\",\"mailSmtpLocalhost\":\"MailSmtpLocalhost\",\"mailSmtpEhlo\":\"MailSmtpEhlo\",\"mailSmtpAuth\":\"MailSmtpAuth\",\"mailSmtpDnsNotify\":\"MailSmtpDnsNotify\",\"mailSmtpDnsRet\":\"MailSmtpDnsRet\",\"mailSmtpAllow8bitmime\":\"MailSmtpAllow8bitmime\",\"mailSmtpSendPartial\":\"MailSmtpSendPartial\",\"emailTemplateDirectory\":\"EmailTemplateDirectory\",\"emailTemplateLogoImage\":\"EmailTemplateLogoImage\",\"emailSubjectLine\":\"EmailSubjectLine\",\"id\":\"Id\"}";
        assertEquals(expectedString, emailConfigRestModel.toString());

        final EmailConfigRestModel emailConfigRestModelNew = new EmailConfigRestModel(id, mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo,
                mailSmtpAuth, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        assertEquals(emailConfigRestModel, emailConfigRestModelNew);
    }
}
