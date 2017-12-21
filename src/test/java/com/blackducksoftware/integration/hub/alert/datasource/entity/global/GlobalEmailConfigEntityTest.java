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
package com.blackducksoftware.integration.hub.alert.datasource.entity.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.mock.EmailMockUtils;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;

public class GlobalEmailConfigEntityTest extends GlobalEntityTest<GlobalEmailConfigEntity> {
    private final EmailMockUtils mockUtils = new EmailMockUtils();

    @Override
    public MockUtils<?, ?, ?, GlobalEmailConfigEntity> getMockUtil() {
        return mockUtils;
    }

    @Override
    public Class<GlobalEmailConfigEntity> getGlobalEntityClass() {
        return GlobalEmailConfigEntity.class;
    }

    @Override
    public void assertGlobalEntityFieldsNull(final GlobalEmailConfigEntity entity) {
        assertNull(entity.getEmailSubjectLine());
        assertNull(entity.getEmailTemplateDirectory());
        assertNull(entity.getEmailTemplateLogoImage());
        assertNull(entity.getMailSmtpAllow8bitmime());
        assertNull(entity.getMailSmtpAuth());
        assertNull(entity.getMailSmtpConnectionTimeout());
        assertNull(entity.getMailSmtpDnsNotify());
        assertNull(entity.getMailSmtpDnsRet());
        assertNull(entity.getMailSmtpEhlo());
        assertNull(entity.getMailSmtpFrom());
        assertNull(entity.getMailSmtpHost());
        assertNull(entity.getMailSmtpLocalhost());
        assertNull(entity.getMailSmtpPassword());
        assertNull(entity.getMailSmtpPort());
        assertNull(entity.getMailSmtpSendPartial());
        assertNull(entity.getMailSmtpTimeout());
        assertNull(entity.getMailSmtpUser());
    }

    @Override
    public long globalEntitySerialId() {
        return GlobalEmailConfigEntity.getSerialversionuid();
    }

    @Override
    public int emptyGlobalEntityHashCode() {
        return 1169169065;
    }

    @Override
    public void assertGlobalEntityFieldsFull(final GlobalEmailConfigEntity entity) {
        assertEquals(mockUtils.getEmailSubjectLine(), entity.getEmailSubjectLine());
        assertEquals(mockUtils.getEmailTemplateDirectory(), entity.getEmailTemplateDirectory());
        assertEquals(mockUtils.getEmailTemplateLogoImage(), entity.getEmailTemplateLogoImage());
        assertEquals(Boolean.valueOf(mockUtils.getMailSmtpAllow8bitmime()), entity.getMailSmtpAllow8bitmime());
        assertEquals(Boolean.valueOf(mockUtils.getMailSmtpAuth()), entity.getMailSmtpAuth());
        assertEquals(Integer.valueOf(mockUtils.getMailSmtpConnectionTimeout()), entity.getMailSmtpConnectionTimeout());
        assertEquals(mockUtils.getMailSmtpDnsNotify(), entity.getMailSmtpDnsNotify());
        assertEquals(mockUtils.getMailSmtpDnsRet(), entity.getMailSmtpDnsRet());
        assertEquals(Boolean.valueOf(mockUtils.getMailSmtpEhlo()), entity.getMailSmtpEhlo());
        assertEquals(mockUtils.getMailSmtpFrom(), entity.getMailSmtpFrom());
        assertEquals(mockUtils.getMailSmtpHost(), entity.getMailSmtpHost());
        assertEquals(mockUtils.getMailSmtpLocalhost(), entity.getMailSmtpLocalhost());
        assertEquals(mockUtils.getMailSmtpPassword(), entity.getMailSmtpPassword());
        assertEquals(Integer.valueOf(mockUtils.getMailSmtpPort()), entity.getMailSmtpPort());
        assertEquals(Boolean.valueOf(mockUtils.getMailSmtpSendPartial()), entity.getMailSmtpSendPartial());
        assertEquals(Integer.valueOf(mockUtils.getMailSmtpTimeout()), entity.getMailSmtpTimeout());
        assertEquals(mockUtils.getMailSmtpUser(), entity.getMailSmtpUser());
    }

    @Override
    public int globalEntityHashCode() {
        return -620287382;
    }

}
