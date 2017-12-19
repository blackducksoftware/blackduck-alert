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
        super(mockUtils, GlobalEmailConfigRestModel.class);
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
        return GlobalEmailConfigRestModel.getSerialversionuid();
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

}
