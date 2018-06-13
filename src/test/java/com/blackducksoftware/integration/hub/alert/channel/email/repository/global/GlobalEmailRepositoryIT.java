package com.blackducksoftware.integration.hub.alert.channel.email.repository.global;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:spring-test.properties")
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalEmailRepositoryIT {

    @Autowired
    GlobalEmailRepository repository;

    @Test
    public void testSaveEntity() {
        // make sure there aren't any entries
        repository.deleteAll();
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

        final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpWriteTimeout, mailSmtpFrom, mailSmtpLocalhost,
                mailSmtpLocalAddress, mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuth, mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable, mailSmtpAuthNtlmDomain,
                mailSmtpAuthNtlmFlags, mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId,
                mailSmtpSaslRealm, mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust, mailSmtpSslProtocols, mailSmtpSslCipherSuites,
                mailSmtpStartTlsEnable, mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort, mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict);
        final GlobalEmailConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final GlobalEmailConfigEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(mailSmtpHost, foundEntity.getMailSmtpHost());
        assertEquals(mailSmtpUser, foundEntity.getMailSmtpUser());
        assertEquals(mailSmtpPassword, foundEntity.getMailSmtpPassword());
        assertEquals(mailSmtpPort, foundEntity.getMailSmtpPort());
        assertEquals(mailSmtpConnectionTimeout, foundEntity.getMailSmtpConnectionTimeout());
        assertEquals(mailSmtpTimeout, foundEntity.getMailSmtpTimeout());
        assertEquals(mailSmtpWriteTimeout, foundEntity.getMailSmtpWriteTimeout());
        assertEquals(mailSmtpFrom, foundEntity.getMailSmtpFrom());
        assertEquals(mailSmtpLocalhost, foundEntity.getMailSmtpLocalhost());
        assertEquals(mailSmtpLocalAddress, foundEntity.getMailSmtpLocalAddress());
        assertEquals(mailSmtpLocalPort, foundEntity.getMailSmtpLocalPort());
        assertEquals(mailSmtpEhlo, foundEntity.getMailSmtpEhlo());
        assertEquals(mailSmtpAuth, foundEntity.getMailSmtpAuth());
        assertEquals(mailSmtpAuthMechanisms, foundEntity.getMailSmtpAuthMechanisms());
        assertEquals(mailSmtpAuthLoginDisable, foundEntity.getMailSmtpAuthLoginDisable());
        assertEquals(mailSmtpAuthPlainDisable, foundEntity.getMailSmtpAuthPlainDisable());
        assertEquals(mailSmtpAuthDigestMd5Disable, foundEntity.getMailSmtpAuthDigestMd5Disable());
        assertEquals(mailSmtpAuthNtlmDisable, foundEntity.getMailSmtpAuthNtlmDisable());
        assertEquals(mailSmtpAuthNtlmDomain, foundEntity.getMailSmtpAuthNtlmDomain());
        assertEquals(mailSmtpAuthNtlmFlags, foundEntity.getMailSmtpAuthNtlmFlags());
        assertEquals(mailSmtpAuthXoauth2Disable, foundEntity.getMailSmtpAuthXoauth2Disable());
        assertEquals(mailSmtpSubmitter, foundEntity.getMailSmtpSubmitter());
        assertEquals(mailSmtpDnsNotify, foundEntity.getMailSmtpDnsNotify());
        assertEquals(mailSmtpDnsRet, foundEntity.getMailSmtpDnsRet());
        assertEquals(mailSmtpAllow8bitmime, foundEntity.getMailSmtpAllow8bitmime());
        assertEquals(mailSmtpSendPartial, foundEntity.getMailSmtpSendPartial());
        assertEquals(mailSmtpSaslEnable, foundEntity.getMailSmtpSaslEnable());
        assertEquals(mailSmtpSaslMechanisms, foundEntity.getMailSmtpSaslMechanisms());
        assertEquals(mailSmtpSaslAuthorizationId, foundEntity.getMailSmtpSaslAuthorizationId());
        assertEquals(mailSmtpSaslRealm, foundEntity.getMailSmtpSaslRealm());
        assertEquals(mailSmtpSaslUseCanonicalHostname, foundEntity.getMailSmtpSaslUseCanonicalHostname());
        assertEquals(mailSmtpQuitwait, foundEntity.getMailSmtpQuitwait());
        assertEquals(mailSmtpReportSuccess, foundEntity.getMailSmtpReportSuccess());
        assertEquals(mailSmtpSslEnable, foundEntity.getMailSmtpSslEnable());
        assertEquals(mailSmtpSslCheckServerIdentity, foundEntity.getMailSmtpSslCheckServerIdentity());
        assertEquals(mailSmtpSslTrust, foundEntity.getMailSmtpSslTrust());
        assertEquals(mailSmtpSslProtocols, foundEntity.getMailSmtpSslProtocols());
        assertEquals(mailSmtpSslCipherSuites, foundEntity.getMailSmtpSslCipherSuites());
        assertEquals(mailSmtpStartTlsEnable, foundEntity.getMailSmtpStartTlsEnable());
        assertEquals(mailSmtpStartTlsRequired, foundEntity.getMailSmtpStartTlsRequired());
        assertEquals(mailSmtpProxyHost, foundEntity.getMailSmtpProxyHost());
        assertEquals(mailSmtpProxyPort, foundEntity.getMailSmtpProxyPort());
        assertEquals(mailSmtpSocksHost, foundEntity.getMailSmtpSocksHost());
        assertEquals(mailSmtpSocksPort, foundEntity.getMailSmtpSocksPort());
        assertEquals(mailSmtpMailExtension, foundEntity.getMailSmtpMailExtension());
        assertEquals(mailSmtpUserSet, foundEntity.getMailSmtpUserSet());
        assertEquals(mailSmtpNoopStrict, foundEntity.getMailSmtpNoopStrict());

    }
}
