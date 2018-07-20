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
package com.blackducksoftware.integration.alert.channel.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.blackducksoftware.integration.alert.common.enumeration.EmailPropertyKeys;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalConfigEntity;

public class EmailPropertiesTest {
    @Test
    public void updateFromConfigTest() {
        final String mailSmtpHost = "smtp_host";
        final String mailSmtpUser = "smtp_user";
        final String mailSmtpPassword = "smtp_password";
        final Integer mailSmtpPort = 80;
        final Integer mailSmtpConnectionTimeout = 100;
        final Integer mailSmtpTimeout = 200;
        final Integer mailSmtpWriteTimeout = 300;
        final String mailSmtpFrom = "smtp_from";
        final String mailSmtpLocalhost = "smtp_localhost";
        final String mailSmtpLocalAddress = "smtp_local_address";
        final Integer mailSmtpLocalPort = 81;
        final Boolean mailSmtpEhlo = true;
        final Boolean mailSmtpAuth = true;
        final String mailSmtpAuthMechanisms = "smtp_auth_mechanisms";
        final Boolean mailSmtpAuthLoginDisable = true;
        final Boolean mailSmtpAuthPlainDisable = true;
        final Boolean mailSmtpAuthDigestMd5Disable = true;
        final Boolean mailSmtpAuthNtlmDisable = true;
        final String mailSmtpAuthNtlmDomain = "smtp_auth_ntlm_domain";
        final Integer mailSmtpAuthNtlmFlags = 1;
        final Boolean mailSmtpAuthXoauth2Disable = true;
        final String mailSmtpSubmitter = "smtp_submitter";
        final String mailSmtpDnsNotify = "smtp_dns_notify";
        final String mailSmtpDnsRet = "smtp_dns_ret";
        final Boolean mailSmtpAllow8bitmime = true;
        final Boolean mailSmtpSendPartial = true;
        final Boolean mailSmtpSaslEnable = true;
        final String mailSmtpSaslMechanisms = "smtp_sasl_mechanisms";
        final String mailSmtpSaslAuthorizationId = "smtp_sasl_authorization_id";
        final String mailSmtpSaslRealm = "smtp_sasl_realm";
        final Boolean mailSmtpSaslUseCanonicalHostname = true;
        final Boolean mailSmtpQuitwait = true;
        final Boolean mailSmtpReportSuccess = true;
        final Boolean mailSmtpSslEnable = true;
        final Boolean mailSmtpSslCheckServerIdentity = true;
        final String mailSmtpSslTrust = "smtp_ssl_trust";
        final String mailSmtpSslProtocols = "smtp_ssl_protocols";
        final String mailSmtpSslCipherSuites = "smtp_ssl_cipher_suites";
        final Boolean mailSmtpStartTlsEnable = true;
        final Boolean mailSmtpStartTlsRequired = true;
        final String mailSmtpProxyHost = "smtp_proxy_host";
        final Integer mailSmtpProxyPort = 82;
        final String mailSmtpSocksHost = "smtp_socks_host";
        final Integer mailSmtpSocksPort = 83;
        final String mailSmtpMailExtension = "smtp_mail_extension";
        final Boolean mailSmtpUserSet = true;
        final Boolean mailSmtpNoopStrict = true;

        final EmailGlobalConfigEntity emailConfigEntity = new EmailGlobalConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpWriteTimeout, mailSmtpFrom,
                mailSmtpLocalhost,
                mailSmtpLocalAddress, mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuth, mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable, mailSmtpAuthNtlmDomain,
                mailSmtpAuthNtlmFlags, mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId,
                mailSmtpSaslRealm, mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust, mailSmtpSslProtocols, mailSmtpSslCipherSuites,
                mailSmtpStartTlsEnable, mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort, mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict);

        final EmailProperties emailProperties = new EmailProperties(emailConfigEntity);
        emailProperties.updateFromConfig(emailConfigEntity);

        assertEquals(mailSmtpUser.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_USER_KEY));
        assertEquals(mailSmtpHost.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_HOST_KEY));
        assertEquals(mailSmtpPort.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_PORT_KEY));
        assertEquals(mailSmtpConnectionTimeout.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY));
        assertEquals(mailSmtpTimeout.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY));
        assertEquals(mailSmtpWriteTimeout.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY));
        assertEquals(mailSmtpFrom.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_FROM_KEY));
        assertEquals(mailSmtpLocalhost.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY));
        assertEquals(mailSmtpLocalAddress.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY));
        assertEquals(mailSmtpLocalPort.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY));
        assertEquals(mailSmtpEhlo.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_EHLO_KEY));
        assertEquals(mailSmtpAuth.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_KEY));
        assertEquals(mailSmtpAuthMechanisms.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY));
        assertEquals(mailSmtpAuthLoginDisable.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY));
        assertEquals(mailSmtpAuthPlainDisable.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY));
        assertEquals(mailSmtpAuthDigestMd5Disable.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY));
        assertEquals(mailSmtpAuthNtlmDisable.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY));
        assertEquals(mailSmtpAuthNtlmDomain.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY));
        assertEquals(mailSmtpAuthNtlmFlags.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY));
        assertEquals(mailSmtpAuthXoauth2Disable.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY));
        assertEquals(mailSmtpSubmitter.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY));
        assertEquals(mailSmtpDnsNotify.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY));
        assertEquals(mailSmtpDnsRet.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY));
        assertEquals(mailSmtpAllow8bitmime.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY));
        assertEquals(mailSmtpSendPartial.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY));
        assertEquals(mailSmtpSaslEnable.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY));
        assertEquals(mailSmtpSaslMechanisms.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY));
        assertEquals(mailSmtpSaslAuthorizationId.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY));
        assertEquals(mailSmtpSaslRealm.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY));
        assertEquals(mailSmtpSaslUseCanonicalHostname.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY));
        assertEquals(mailSmtpQuitwait.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY));
        assertEquals(mailSmtpReportSuccess.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY));
        assertEquals(mailSmtpSslEnable.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY));
        assertEquals(mailSmtpSslCheckServerIdentity.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY));
        assertEquals(mailSmtpSslTrust.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY));
        assertEquals(mailSmtpSslProtocols.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY));
        assertEquals(mailSmtpSslCipherSuites.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY));
        assertEquals(mailSmtpStartTlsEnable.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY));
        assertEquals(mailSmtpStartTlsRequired.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY));
        assertEquals(mailSmtpProxyHost.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY));
        assertEquals(mailSmtpProxyPort.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY));
        assertEquals(mailSmtpSocksHost.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY));
        assertEquals(mailSmtpSocksPort.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY));
        assertEquals(mailSmtpMailExtension.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY));
        assertEquals(mailSmtpUserSet.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_USERSET_KEY));
        assertEquals(mailSmtpNoopStrict.toString(), emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY));
        assertEquals(mailSmtpPassword.toString(), emailProperties.getMailSmtpPassword());
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
