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
package com.synopsys.integration.alert.channel.email.model;

import static org.junit.Assert.*;

import com.synopsys.integration.alert.channel.email.mock.MockEmailGlobalRestModel;
import com.synopsys.integration.alert.web.channel.model.EmailGlobalConfig;
import com.synopsys.integration.alert.web.model.GlobalRestModelTest;

public class GlobalEmailConfigRestModelTest extends GlobalRestModelTest<EmailGlobalConfig> {

    @Override
    public void assertGlobalRestModelFieldsNull(final EmailGlobalConfig restModel) {
        assertNull(restModel.getMailSmtpHost());
        assertNull(restModel.getMailSmtpUser());
        assertNull(restModel.getMailSmtpPassword());
        assertNull(restModel.getMailSmtpPort());
        assertNull(restModel.getMailSmtpConnectionTimeout());
        assertNull(restModel.getMailSmtpTimeout());
        assertNull(restModel.getMailSmtpWriteTimeout());
        assertNull(restModel.getMailSmtpFrom());
        assertNull(restModel.getMailSmtpLocalhost());
        assertNull(restModel.getMailSmtpLocalAddress());
        assertNull(restModel.getMailSmtpLocalPort());
        assertFalse(restModel.getMailSmtpEhlo());
        assertFalse(restModel.getMailSmtpAuth());
        assertNull(restModel.getMailSmtpAuthMechanisms());
        assertFalse(restModel.getMailSmtpAuthLoginDisable());
        assertFalse(restModel.getMailSmtpAuthPlainDisable());
        assertFalse(restModel.getMailSmtpAuthDigestMd5Disable());
        assertFalse(restModel.getMailSmtpAuthNtlmDisable());
        assertNull(restModel.getMailSmtpAuthNtlmDomain());
        assertNull(restModel.getMailSmtpAuthNtlmFlags());
        assertFalse(restModel.getMailSmtpAuthXoauth2Disable());
        assertNull(restModel.getMailSmtpSubmitter());
        assertNull(restModel.getMailSmtpDnsNotify());
        assertNull(restModel.getMailSmtpDnsRet());
        assertFalse(restModel.getMailSmtpAllow8bitmime());
        assertFalse(restModel.getMailSmtpSendPartial());
        assertFalse(restModel.getMailSmtpSaslEnable());
        assertNull(restModel.getMailSmtpSaslMechanisms());
        assertNull(restModel.getMailSmtpSaslAuthorizationId());
        assertNull(restModel.getMailSmtpSaslRealm());
        assertFalse(restModel.getMailSmtpSaslUseCanonicalHostname());
        assertFalse(restModel.getMailSmtpQuitwait());
        assertFalse(restModel.getMailSmtpReportSuccess());
        assertFalse(restModel.getMailSmtpSslEnable());
        assertFalse(restModel.getMailSmtpSslCheckServerIdentity());
        assertNull(restModel.getMailSmtpSslTrust());
        assertNull(restModel.getMailSmtpSslProtocols());
        assertNull(restModel.getMailSmtpSslCipherSuites());
        assertFalse(restModel.getMailSmtpStartTlsEnable());
        assertFalse(restModel.getMailSmtpStartTlsRequired());
        assertNull(restModel.getMailSmtpProxyHost());
        assertNull(restModel.getMailSmtpProxyPort());
        assertNull(restModel.getMailSmtpSocksHost());
        assertNull(restModel.getMailSmtpSocksPort());
        assertNull(restModel.getMailSmtpMailExtension());
        assertFalse(restModel.getMailSmtpUserSet());
        assertFalse(restModel.getMailSmtpNoopStrict());
        assertTrue(!restModel.isMailSmtpPasswordIsSet());
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final EmailGlobalConfig restModel) {
        assertEquals(getMockUtil().getMailSmtpHost(), restModel.getMailSmtpHost());
        assertEquals(getMockUtil().getMailSmtpUser(), restModel.getMailSmtpUser());
        assertEquals(getMockUtil().getMailSmtpPassword(), restModel.getMailSmtpPassword());
        assertEquals(getMockUtil().getMailSmtpPort(), restModel.getMailSmtpPort());
        assertEquals(getMockUtil().getMailSmtpConnectionTimeout(), restModel.getMailSmtpConnectionTimeout());
        assertEquals(getMockUtil().getMailSmtpTimeout(), restModel.getMailSmtpTimeout());
        assertEquals(getMockUtil().getMailSmtpWriteTimeout(), restModel.getMailSmtpWriteTimeout());
        assertEquals(getMockUtil().getMailSmtpFrom(), restModel.getMailSmtpFrom());
        assertEquals(getMockUtil().getMailSmtpLocalhost(), restModel.getMailSmtpLocalhost());
        assertEquals(getMockUtil().getMailSmtpLocalAddress(), restModel.getMailSmtpLocalAddress());
        assertEquals(getMockUtil().getMailSmtpLocalPort(), restModel.getMailSmtpLocalPort());
        assertEquals(getMockUtil().getMailSmtpEhlo(), restModel.getMailSmtpEhlo());
        assertEquals(getMockUtil().getMailSmtpAuth(), restModel.getMailSmtpAuth());
        assertEquals(getMockUtil().getMailSmtpAuthMechanisms(), restModel.getMailSmtpAuthMechanisms());
        assertEquals(getMockUtil().getMailSmtpAuthLoginDisable(), restModel.getMailSmtpAuthLoginDisable());
        assertEquals(getMockUtil().getMailSmtpAuthPlainDisable(), restModel.getMailSmtpAuthPlainDisable());
        assertEquals(getMockUtil().getMailSmtpAuthDigestMd5Disable(), restModel.getMailSmtpAuthDigestMd5Disable());
        assertEquals(getMockUtil().getMailSmtpAuthNtlmDisable(), restModel.getMailSmtpAuthNtlmDisable());
        assertEquals(getMockUtil().getMailSmtpAuthNtlmDomain(), restModel.getMailSmtpAuthNtlmDomain());
        assertEquals(getMockUtil().getMailSmtpAuthNtlmFlags(), restModel.getMailSmtpAuthNtlmFlags());
        assertEquals(getMockUtil().getMailSmtpAuthXoauth2Disable(), restModel.getMailSmtpAuthXoauth2Disable());
        assertEquals(getMockUtil().getMailSmtpSubmitter(), restModel.getMailSmtpSubmitter());
        assertEquals(getMockUtil().getMailSmtpDnsNotify(), restModel.getMailSmtpDnsNotify());
        assertEquals(getMockUtil().getMailSmtpDnsRet(), restModel.getMailSmtpDnsRet());
        assertEquals(getMockUtil().getMailSmtpAllow8bitmime(), restModel.getMailSmtpAllow8bitmime());
        assertEquals(getMockUtil().getMailSmtpSendPartial(), restModel.getMailSmtpSendPartial());
        assertEquals(getMockUtil().getMailSmtpSaslEnable(), restModel.getMailSmtpSaslEnable());
        assertEquals(getMockUtil().getMailSmtpSaslMechanisms(), restModel.getMailSmtpSaslMechanisms());
        assertEquals(getMockUtil().getMailSmtpSaslAuthorizationId(), restModel.getMailSmtpSaslAuthorizationId());
        assertEquals(getMockUtil().getMailSmtpSaslRealm(), restModel.getMailSmtpSaslRealm());
        assertEquals(getMockUtil().getMailSmtpSaslUseCanonicalHostname(), restModel.getMailSmtpSaslUseCanonicalHostname());
        assertEquals(getMockUtil().getMailSmtpQuitwait(), restModel.getMailSmtpQuitwait());
        assertEquals(getMockUtil().getMailSmtpReportSuccess(), restModel.getMailSmtpReportSuccess());
        assertEquals(getMockUtil().getMailSmtpSslEnable(), restModel.getMailSmtpSslEnable());
        assertEquals(getMockUtil().getMailSmtpSslCheckServerIdentity(), restModel.getMailSmtpSslCheckServerIdentity());
        assertEquals(getMockUtil().getMailSmtpSslTrust(), restModel.getMailSmtpSslTrust());
        assertEquals(getMockUtil().getMailSmtpSslProtocols(), restModel.getMailSmtpSslProtocols());
        assertEquals(getMockUtil().getMailSmtpSslCipherSuites(), restModel.getMailSmtpSslCipherSuites());
        assertEquals(getMockUtil().getMailSmtpStartTlsEnable(), restModel.getMailSmtpStartTlsEnable());
        assertEquals(getMockUtil().getMailSmtpStartTlsRequired(), restModel.getMailSmtpStartTlsRequired());
        assertEquals(getMockUtil().getMailSmtpProxyHost(), restModel.getMailSmtpProxyHost());
        assertEquals(getMockUtil().getMailSmtpProxyPort(), restModel.getMailSmtpProxyPort());
        assertEquals(getMockUtil().getMailSmtpSocksHost(), restModel.getMailSmtpSocksHost());
        assertEquals(getMockUtil().getMailSmtpSocksPort(), restModel.getMailSmtpSocksPort());
        assertEquals(getMockUtil().getMailSmtpMailExtension(), restModel.getMailSmtpMailExtension());
        assertEquals(getMockUtil().getMailSmtpUserSet(), restModel.getMailSmtpUserSet());
        assertEquals(getMockUtil().getMailSmtpNoopStrict(), restModel.getMailSmtpNoopStrict());

        assertEquals(getMockUtil().isMailSmtpPasswordIsSet(), restModel.isMailSmtpPasswordIsSet());
    }

    @Override
    public Class<EmailGlobalConfig> getGlobalRestModelClass() {
        return EmailGlobalConfig.class;
    }

    @Override
    public MockEmailGlobalRestModel getMockUtil() {
        return new MockEmailGlobalRestModel();
    }

}
