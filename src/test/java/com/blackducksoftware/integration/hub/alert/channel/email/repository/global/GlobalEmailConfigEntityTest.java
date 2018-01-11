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
package com.blackducksoftware.integration.hub.alert.channel.email.repository.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalEntityTest;

public class GlobalEmailConfigEntityTest extends GlobalEntityTest<GlobalEmailConfigEntity> {

    @Override
    public MockEmailGlobalEntity getMockUtil() {
        return new MockEmailGlobalEntity();
    }

    @Override
    public Class<GlobalEmailConfigEntity> getGlobalEntityClass() {
        return GlobalEmailConfigEntity.class;
    }

    @Override
    public void assertGlobalEntityFieldsNull(final GlobalEmailConfigEntity entity) {
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
        return 1089599805;
    }

    @Override
    public void assertGlobalEntityFieldsFull(final GlobalEmailConfigEntity entity) {
        assertEquals(getMockUtil().getMailSmtpAllow8bitmime(), entity.getMailSmtpAllow8bitmime());
        assertEquals(getMockUtil().getMailSmtpAuth(), entity.getMailSmtpAuth());
        assertEquals(getMockUtil().getMailSmtpConnectionTimeout(), entity.getMailSmtpConnectionTimeout());
        assertEquals(getMockUtil().getMailSmtpDnsNotify(), entity.getMailSmtpDnsNotify());
        assertEquals(getMockUtil().getMailSmtpDnsRet(), entity.getMailSmtpDnsRet());
        assertEquals(getMockUtil().getMailSmtpEhlo(), entity.getMailSmtpEhlo());
        assertEquals(getMockUtil().getMailSmtpFrom(), entity.getMailSmtpFrom());
        assertEquals(getMockUtil().getMailSmtpHost(), entity.getMailSmtpHost());
        assertEquals(getMockUtil().getMailSmtpLocalhost(), entity.getMailSmtpLocalhost());
        assertEquals(getMockUtil().getMailSmtpPassword(), entity.getMailSmtpPassword());
        assertEquals(getMockUtil().getMailSmtpPort(), entity.getMailSmtpPort());
        assertEquals(getMockUtil().getMailSmtpSendPartial(), entity.getMailSmtpSendPartial());
        assertEquals(getMockUtil().getMailSmtpTimeout(), entity.getMailSmtpTimeout());
        assertEquals(getMockUtil().getMailSmtpUser(), entity.getMailSmtpUser());
    }

    @Override
    public int globalEntityHashCode() {
        return -1115978415;
    }

}
