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
package com.blackducksoftware.integration.alert.channel.email.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.GlobalEntityTest;

public class GlobalEmailConfigEntityTest extends GlobalEntityTest<EmailGlobalConfigEntity> {

    @Override
    public MockEmailGlobalEntity getMockUtil() {
        return new MockEmailGlobalEntity();
    }

    @Override
    public Class<EmailGlobalConfigEntity> getGlobalEntityClass() {
        return EmailGlobalConfigEntity.class;
    }

    @Override
    public void assertGlobalEntityFieldsNull(final EmailGlobalConfigEntity entity) {
        assertNull(entity.getMailSmtpHost());
        assertNull(entity.getMailSmtpUser());
        assertNull(entity.getMailSmtpPassword());
        assertNull(entity.getMailSmtpPort());
        assertNull(entity.getMailSmtpConnectionTimeout());
        assertNull(entity.getMailSmtpTimeout());
        assertNull(entity.getMailSmtpWriteTimeout());
        assertNull(entity.getMailSmtpFrom());
        assertNull(entity.getMailSmtpLocalhost());
        assertNull(entity.getMailSmtpLocalAddress());
        assertNull(entity.getMailSmtpLocalPort());
        assertNull(entity.getMailSmtpEhlo());
        assertNull(entity.getMailSmtpAuth());
        assertNull(entity.getMailSmtpAuthMechanisms());
        assertNull(entity.getMailSmtpAuthLoginDisable());
        assertNull(entity.getMailSmtpAuthPlainDisable());
        assertNull(entity.getMailSmtpAuthDigestMd5Disable());
        assertNull(entity.getMailSmtpAuthNtlmDisable());
        assertNull(entity.getMailSmtpAuthNtlmDomain());
        assertNull(entity.getMailSmtpAuthNtlmFlags());
        assertNull(entity.getMailSmtpAuthXoauth2Disable());
        assertNull(entity.getMailSmtpSubmitter());
        assertNull(entity.getMailSmtpDnsNotify());
        assertNull(entity.getMailSmtpDnsRet());
        assertNull(entity.getMailSmtpAllow8bitmime());
        assertNull(entity.getMailSmtpSendPartial());
        assertNull(entity.getMailSmtpSaslEnable());
        assertNull(entity.getMailSmtpSaslMechanisms());
        assertNull(entity.getMailSmtpSaslAuthorizationId());
        assertNull(entity.getMailSmtpSaslRealm());
        assertNull(entity.getMailSmtpSaslUseCanonicalHostname());
        assertNull(entity.getMailSmtpQuitwait());
        assertNull(entity.getMailSmtpReportSuccess());
        assertNull(entity.getMailSmtpSslEnable());
        assertNull(entity.getMailSmtpSslCheckServerIdentity());
        assertNull(entity.getMailSmtpSslTrust());
        assertNull(entity.getMailSmtpSslProtocols());
        assertNull(entity.getMailSmtpSslCipherSuites());
        assertNull(entity.getMailSmtpStartTlsEnable());
        assertNull(entity.getMailSmtpStartTlsRequired());
        assertNull(entity.getMailSmtpProxyHost());
        assertNull(entity.getMailSmtpProxyPort());
        assertNull(entity.getMailSmtpSocksHost());
        assertNull(entity.getMailSmtpSocksPort());
        assertNull(entity.getMailSmtpMailExtension());
        assertNull(entity.getMailSmtpUserSet());
        assertNull(entity.getMailSmtpNoopStrict());
    }

    @Override
    public void assertGlobalEntityFieldsFull(final EmailGlobalConfigEntity entity) {
        assertEquals(getMockUtil().getMailSmtpHost(), entity.getMailSmtpHost());
        assertEquals(getMockUtil().getMailSmtpUser(), entity.getMailSmtpUser());
        assertEquals(getMockUtil().getMailSmtpPassword(), entity.getMailSmtpPassword());
        assertEquals(getMockUtil().getMailSmtpPort(), entity.getMailSmtpPort());
        assertEquals(getMockUtil().getMailSmtpConnectionTimeout(), entity.getMailSmtpConnectionTimeout());
        assertEquals(getMockUtil().getMailSmtpTimeout(), entity.getMailSmtpTimeout());
        assertEquals(getMockUtil().getMailSmtpWriteTimeout(), entity.getMailSmtpWriteTimeout());
        assertEquals(getMockUtil().getMailSmtpFrom(), entity.getMailSmtpFrom());
        assertEquals(getMockUtil().getMailSmtpLocalhost(), entity.getMailSmtpLocalhost());
        assertEquals(getMockUtil().getMailSmtpLocalAddress(), entity.getMailSmtpLocalAddress());
        assertEquals(getMockUtil().getMailSmtpLocalPort(), entity.getMailSmtpLocalPort());
        assertEquals(getMockUtil().getMailSmtpEhlo(), entity.getMailSmtpEhlo());
        assertEquals(getMockUtil().getMailSmtpAuth(), entity.getMailSmtpAuth());
        assertEquals(getMockUtil().getMailSmtpAuthMechanisms(), entity.getMailSmtpAuthMechanisms());
        assertEquals(getMockUtil().getMailSmtpAuthLoginDisable(), entity.getMailSmtpAuthLoginDisable());
        assertEquals(getMockUtil().getMailSmtpAuthPlainDisable(), entity.getMailSmtpAuthPlainDisable());
        assertEquals(getMockUtil().getMailSmtpAuthDigestMd5Disable(), entity.getMailSmtpAuthDigestMd5Disable());
        assertEquals(getMockUtil().getMailSmtpAuthNtlmDisable(), entity.getMailSmtpAuthNtlmDisable());
        assertEquals(getMockUtil().getMailSmtpAuthNtlmDomain(), entity.getMailSmtpAuthNtlmDomain());
        assertEquals(getMockUtil().getMailSmtpAuthNtlmFlags(), entity.getMailSmtpAuthNtlmFlags());
        assertEquals(getMockUtil().getMailSmtpAuthXoauth2Disable(), entity.getMailSmtpAuthXoauth2Disable());
        assertEquals(getMockUtil().getMailSmtpSubmitter(), entity.getMailSmtpSubmitter());
        assertEquals(getMockUtil().getMailSmtpDnsNotify(), entity.getMailSmtpDnsNotify());
        assertEquals(getMockUtil().getMailSmtpDnsRet(), entity.getMailSmtpDnsRet());
        assertEquals(getMockUtil().getMailSmtpAllow8bitmime(), entity.getMailSmtpAllow8bitmime());
        assertEquals(getMockUtil().getMailSmtpSendPartial(), entity.getMailSmtpSendPartial());
        assertEquals(getMockUtil().getMailSmtpSaslEnable(), entity.getMailSmtpSaslEnable());
        assertEquals(getMockUtil().getMailSmtpSaslMechanisms(), entity.getMailSmtpSaslMechanisms());
        assertEquals(getMockUtil().getMailSmtpSaslAuthorizationId(), entity.getMailSmtpSaslAuthorizationId());
        assertEquals(getMockUtil().getMailSmtpSaslRealm(), entity.getMailSmtpSaslRealm());
        assertEquals(getMockUtil().getMailSmtpSaslUseCanonicalHostname(), entity.getMailSmtpSaslUseCanonicalHostname());
        assertEquals(getMockUtil().getMailSmtpQuitwait(), entity.getMailSmtpQuitwait());
        assertEquals(getMockUtil().getMailSmtpReportSuccess(), entity.getMailSmtpReportSuccess());
        assertEquals(getMockUtil().getMailSmtpSslEnable(), entity.getMailSmtpSslEnable());
        assertEquals(getMockUtil().getMailSmtpSslCheckServerIdentity(), entity.getMailSmtpSslCheckServerIdentity());
        assertEquals(getMockUtil().getMailSmtpSslTrust(), entity.getMailSmtpSslTrust());
        assertEquals(getMockUtil().getMailSmtpSslProtocols(), entity.getMailSmtpSslProtocols());
        assertEquals(getMockUtil().getMailSmtpSslCipherSuites(), entity.getMailSmtpSslCipherSuites());
        assertEquals(getMockUtil().getMailSmtpStartTlsEnable(), entity.getMailSmtpStartTlsEnable());
        assertEquals(getMockUtil().getMailSmtpStartTlsRequired(), entity.getMailSmtpStartTlsRequired());
        assertEquals(getMockUtil().getMailSmtpProxyHost(), entity.getMailSmtpProxyHost());
        assertEquals(getMockUtil().getMailSmtpProxyPort(), entity.getMailSmtpProxyPort());
        assertEquals(getMockUtil().getMailSmtpSocksHost(), entity.getMailSmtpSocksHost());
        assertEquals(getMockUtil().getMailSmtpSocksPort(), entity.getMailSmtpSocksPort());
        assertEquals(getMockUtil().getMailSmtpMailExtension(), entity.getMailSmtpMailExtension());
        assertEquals(getMockUtil().getMailSmtpUserSet(), entity.getMailSmtpUserSet());
        assertEquals(getMockUtil().getMailSmtpNoopStrict(), entity.getMailSmtpNoopStrict());
    }

}
