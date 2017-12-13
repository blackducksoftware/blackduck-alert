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
package com.blackducksoftware.integration.hub.alert.web.model.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.EmailMockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.EmailGroupDistributionRestModel;

public class GlobalEmailConfigRestModelTest extends GlobalRestModelTest<EmailGroupDistributionRestModel, GlobalEmailConfigRestModel, EmailGroupDistributionConfigEntity, GlobalEmailConfigEntity> {
    private static final EmailMockUtils mockUtils = new EmailMockUtils();

    public GlobalEmailConfigRestModelTest() {
        super(mockUtils);
    }

    @Override
    public void assertGlobalRestModelFieldsNull(final GlobalEmailConfigRestModel restModel) {
        assertNull(restModel.getEmailSubjectLine());
        assertNull(restModel.getEmailTemplateDirectory());
        assertNull(restModel.getEmailTemplateLogoImage());
        assertNull(restModel.getId());
        assertNull(restModel.getMailSmtpAllow8bitmime());
        assertNull(restModel.getMailSmtpAuth());
        assertNull(restModel.getMailSmtpConnectionTimeout());
        assertNull(restModel.getMailSmtpDnsNotify());
        assertNull(restModel.getMailSmtpDnsRet());
        assertNull(restModel.getMailSmtpEhlo());
        assertNull(restModel.getMailSmtpFrom());
        assertNull(restModel.getMailSmtpHost());
        assertNull(restModel.getMailSmtpLocalhost());
        assertNull(restModel.getMailSmtpPassword());
        assertNull(restModel.getMailSmtpPort());
        assertNull(restModel.getMailSmtpSendPartial());
        assertNull(restModel.getMailSmtpTimeout());
        assertNull(restModel.getMailSmtpUser());
    }

    @Override
    public long emptyGlobalRestModelSerialId() {
        return 3607759169675906880L;
    }

    @Override
    public int emptyGlobalRestModelHashCode() {
        return 1169169065;
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final GlobalEmailConfigRestModel restModel) {
        assertEquals(mockUtils.getEmailSubjectLine(), restModel.getEmailSubjectLine());
        assertEquals(mockUtils.getEmailTemplateDirectory(), restModel.getEmailTemplateDirectory());
        assertEquals(mockUtils.getEmailTemplateLogoImage(), restModel.getEmailTemplateLogoImage());
        assertEquals(mockUtils.getMailSmtpAllow8bitmime(), restModel.getMailSmtpAllow8bitmime());
        assertEquals(mockUtils.getMailSmtpAuth(), restModel.getMailSmtpAuth());
        assertEquals(mockUtils.getMailSmtpConnectionTimeout(), restModel.getMailSmtpConnectionTimeout());
        assertEquals(mockUtils.getMailSmtpDnsNotify(), restModel.getMailSmtpDnsNotify());
        assertEquals(mockUtils.getMailSmtpDnsRet(), restModel.getMailSmtpDnsRet());
        assertEquals(mockUtils.getMailSmtpEhlo(), restModel.getMailSmtpEhlo());
        assertEquals(mockUtils.getMailSmtpFrom(), restModel.getMailSmtpFrom());
        assertEquals(mockUtils.getMailSmtpHost(), restModel.getMailSmtpHost());
        assertEquals(mockUtils.getMailSmtpLocalhost(), restModel.getMailSmtpLocalhost());
        assertEquals(mockUtils.getMailSmtpPassword(), restModel.getMailSmtpPassword());
        assertEquals(mockUtils.getMailSmtpPort(), restModel.getMailSmtpPort());
        assertEquals(mockUtils.getMailSmtpSendPartial(), restModel.getMailSmtpSendPartial());
        assertEquals(mockUtils.getMailSmtpTimeout(), restModel.getMailSmtpTimeout());
        assertEquals(mockUtils.getMailSmtpUser(), restModel.getMailSmtpUser());
    }

    @Override
    public int gloablRestModelHashCode() {
        return -1349295802;
    }

    // @Test
    // public void testEmptyModel() {
    // final GlobalEmailConfigRestModel emailConfigRestModel = new GlobalEmailConfigRestModel();
    // assertEquals(9172607945030111585L, GlobalEmailConfigRestModel.getSerialversionuid());
    //
    // assertNull(emailConfigRestModel.getEmailSubjectLine());
    // assertNull(emailConfigRestModel.getEmailTemplateDirectory());
    // assertNull(emailConfigRestModel.getEmailTemplateLogoImage());
    // assertNull(emailConfigRestModel.getId());
    // assertNull(emailConfigRestModel.getMailSmtpAllow8bitmime());
    // assertNull(emailConfigRestModel.getMailSmtpAuth());
    // assertNull(emailConfigRestModel.getMailSmtpConnectionTimeout());
    // assertNull(emailConfigRestModel.getMailSmtpDnsNotify());
    // assertNull(emailConfigRestModel.getMailSmtpDnsRet());
    // assertNull(emailConfigRestModel.getMailSmtpEhlo());
    // assertNull(emailConfigRestModel.getMailSmtpFrom());
    // assertNull(emailConfigRestModel.getMailSmtpHost());
    // assertNull(emailConfigRestModel.getMailSmtpLocalhost());
    // assertNull(emailConfigRestModel.getMailSmtpPassword());
    // assertNull(emailConfigRestModel.getMailSmtpPort());
    // assertNull(emailConfigRestModel.getMailSmtpSendPartial());
    // assertNull(emailConfigRestModel.getMailSmtpTimeout());
    // assertNull(emailConfigRestModel.getMailSmtpUser());
    //
    // assertEquals(1169169065, emailConfigRestModel.hashCode());
    //
    // final String expectedString = mockUtils.getEmptyGlobalEmailConfigRestModelJson();
    // assertEquals(expectedString, emailConfigRestModel.toString());
    //
    // final GlobalEmailConfigRestModel emailConfigRestModelNew = new GlobalEmailConfigRestModel();
    // assertEquals(emailConfigRestModel, emailConfigRestModelNew);
    // }
    //
    // // @Test
    // public void testModel() {
    // final String id = "1";
    // final String mailSmtpHost = "MailSmtpHost";
    // final String mailSmtpUser = "MailSmtpUser";
    // final String mailSmtpPassword = "MailSmtpPassword";
    // final String mailSmtpPort = "MailSmtpPort";
    // final String mailSmtpConnectionTimeout = "MailSmtpConnectionTimeout";
    // final String mailSmtpTimeout = "MailSmtpTimeout";
    // final String mailSmtpFrom = "MailSmtpFrom";
    // final String mailSmtpLocalhost = "MailSmtpLocalhost";
    // final String mailSmtpEhlo = "MailSmtpEhlo";
    // final String mailSmtpAuth = "MailSmtpAuth";
    // final String mailSmtpDnsNotify = "MailSmtpDnsNotify";
    // final String mailSmtpDnsRet = "MailSmtpDnsRet";
    // final String mailSmtpAllow8bitmime = "MailSmtpAllow8bitmime";
    // final String mailSmtpSendPartial = "MailSmtpSendPartial";
    // final String emailTemplateDirectory = "EmailTemplateDirectory";
    // final String emailTemplateLogoImage = "EmailTemplateLogoImage";
    // final String emailSubjectLine = "EmailSubjectLine";
    //
    // final GlobalEmailConfigRestModel emailConfigRestModel = mockUtils.createGlobalEmailConfigRestModel();
    // assertEquals(emailSubjectLine, emailConfigRestModel.getEmailSubjectLine());
    // assertEquals(emailTemplateDirectory, emailConfigRestModel.getEmailTemplateDirectory());
    // assertEquals(emailTemplateLogoImage, emailConfigRestModel.getEmailTemplateLogoImage());
    // assertEquals(id, emailConfigRestModel.getId());
    // assertEquals(mailSmtpAllow8bitmime, emailConfigRestModel.getMailSmtpAllow8bitmime());
    // assertEquals(mailSmtpAuth, emailConfigRestModel.getMailSmtpAuth());
    // assertEquals(mailSmtpConnectionTimeout, emailConfigRestModel.getMailSmtpConnectionTimeout());
    // assertEquals(mailSmtpDnsNotify, emailConfigRestModel.getMailSmtpDnsNotify());
    // assertEquals(mailSmtpDnsRet, emailConfigRestModel.getMailSmtpDnsRet());
    // assertEquals(mailSmtpEhlo, emailConfigRestModel.getMailSmtpEhlo());
    // assertEquals(mailSmtpFrom, emailConfigRestModel.getMailSmtpFrom());
    // assertEquals(mailSmtpHost, emailConfigRestModel.getMailSmtpHost());
    // assertEquals(mailSmtpLocalhost, emailConfigRestModel.getMailSmtpLocalhost());
    // assertEquals(mailSmtpPassword, emailConfigRestModel.getMailSmtpPassword());
    // assertEquals(mailSmtpPort, emailConfigRestModel.getMailSmtpPort());
    // assertEquals(mailSmtpSendPartial, emailConfigRestModel.getMailSmtpSendPartial());
    // assertEquals(mailSmtpTimeout, emailConfigRestModel.getMailSmtpTimeout());
    // assertEquals(mailSmtpUser, emailConfigRestModel.getMailSmtpUser());
    //
    // assertEquals(866212366, emailConfigRestModel.hashCode());
    //
    // final String expectedString = mockUtils.getGlobalEmailConfigRestModelJson();
    // assertEquals(expectedString, emailConfigRestModel.toString());
    //
    // final GlobalEmailConfigRestModel emailConfigRestModelNew = mockUtils.createGlobalEmailConfigRestModel();
    // assertEquals(emailConfigRestModel, emailConfigRestModelNew);
    // }
}
