package com.blackducksoftware.integration.hub.alert.channel.email.repository.global;

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
        final String mailSmtpHost = "smtp_host";
        final String mailSmtpUser = "smtp_user";
        final String mailSmtpPassword = "smtp_password";
        final Integer mailSmtpPort;
        final Integer mailSmtpConnectionTimeout;
        final Integer mailSmtpTimeout;
        final Integer mailSmtpWriteTimeout;
        final String mailSmtpFrom;
        final String mailSmtpLocalhost;
        final String mailSmtpLocalAddress;
        final Integer mailSmtpLocalPort;
        final Boolean mailSmtpEhlo;
        final Boolean mailSmtpAuth;
        final String mailSmtpAuthMechanisms;
        final Boolean mailSmtpAuthLoginDisable;
        final Boolean mailSmtpAuthPlainDisable;
        final Boolean mailSmtpAuthDigestMd5Disable;
        final Boolean mailSmtpAuthNtlmDisable;
        final String mailSmtpAuthNtlmDomain;
        final Integer mailSmtpAuthNtlmFlags;
        final Boolean mailSmtpAuthXoauth2Disable;
        final String mailSmtpSubmitter;
        final String mailSmtpDnsNotify;
        final String mailSmtpDnsRet;
        final Boolean mailSmtpAllow8bitmime;
        final Boolean mailSmtpSendPartial;
        final Boolean mailSmtpSaslEnable;
        final String mailSmtpSaslMechanisms;
        final String mailSmtpSaslAuthorizationId;
        final String mailSmtpSaslRealm;
        final Boolean mailSmtpSaslUseCanonicalHostname;
        final Boolean mailSmtpQuitwait;
        final Boolean mailSmtpReportSuccess;
        final Boolean mailSmtpSslEnable;
        final Boolean mailSmtpSslCheckServerIdentity;
        final String mailSmtpSslTrust;
        final String mailSmtpSslProtocols;
        final String mailSmtpSslCipherSuites;
        final Boolean mailSmtpStartTlsEnable;
        final Boolean mailSmtpStartTlsRequired;
        final String mailSmtpProxyHost;
        final Integer mailSmtpProxyPort;
        final String mailSmtpSocksHost;
        final Integer mailSmtpSocksPort;
        final String mailSmtpMailExtension;
        final Boolean mailSmtpUserSet;
        final Boolean mailSmtpNoopStrict;
        // TODO fix

        // final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpWriteTimeout, mailSmtpFrom, mailSmtpLocalhost,
        // mailSmtpLocalAddress, mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuth, mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable, mailSmtpAuthNtlmDomain,
        // mailSmtpAuthNtlmFlags, mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId,
        // mailSmtpSaslRealm, mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust, mailSmtpSslProtocols, mailSmtpSslCipherSuites,
        // mailSmtpStartTlsEnable, mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort, mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict);
        // final GlobalEmailConfigEntity savedEntity = repository.save(entity);
        // final long count = repository.count();
        // assertEquals(1, count);
        // final GlobalEmailConfigEntity foundEntity = repository.findOne(savedEntity.getId());
        // assertEquals(mailSmtpHost, foundEntity.getMailSmtpHost());
        // assertEquals(mailSmtpUser, foundEntity.getMailSmtpUser());
        // assertEquals(mailSmtpPassword, foundEntity.getMailSmtpPassword());
        // assertEquals(mailSmtpPort, foundEntity.getMailSmtpPort());
        // assertEquals(mailSmtpConnectionTimeout, foundEntity.getMailSmtpConnectionTimeout());
        // assertEquals(mailSmtpTimeout, foundEntity.getMailSmtpTimeout());
        // assertEquals(mailSmtpFrom, foundEntity.getMailSmtpFrom());
        // assertEquals(mailSmtpLocalhost, foundEntity.getMailSmtpLocalhost());
        // assertEquals(mailSmtpEhlo, foundEntity.getMailSmtpEhlo());
        // assertEquals(mailSmtpAuth, foundEntity.getMailSmtpAuth());
        // assertEquals(mailSmtpDnsNotify, foundEntity.getMailSmtpDnsNotify());
        // assertEquals(mailSmtpDnsRet, foundEntity.getMailSmtpDnsRet());
        // assertEquals(mailSmtpAllow8bitmime, foundEntity.getMailSmtpAllow8bitmime());
        // assertEquals(mailSmtpSendPartial, foundEntity.getMailSmtpSendPartial());

    }
}
