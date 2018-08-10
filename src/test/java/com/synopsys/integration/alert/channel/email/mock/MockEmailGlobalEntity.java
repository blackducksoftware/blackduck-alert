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
package com.synopsys.integration.alert.channel.email.mock;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.mock.MockGlobalEntityUtil;

public class MockEmailGlobalEntity extends MockGlobalEntityUtil<EmailGlobalConfigEntity> {
    private String mailSmtpHost;
    private String mailSmtpUser;
    private String mailSmtpPassword;
    private boolean mailSmtpPasswordIsSet;
    private Integer mailSmtpPort;
    private Integer mailSmtpConnectionTimeout;
    private Integer mailSmtpTimeout;
    private final Integer mailSmtpWriteTimeout;
    private String mailSmtpFrom;
    private String mailSmtpLocalhost;
    private final String mailSmtpLocalAddress;
    private final Integer mailSmtpLocalPort;
    private Boolean mailSmtpEhlo;
    private Boolean mailSmtpAuth;
    private final String mailSmtpAuthMechanisms;
    private final Boolean mailSmtpAuthLoginDisable;
    private final Boolean mailSmtpAuthPlainDisable;
    private final Boolean mailSmtpAuthDigestMd5Disable;
    private final Boolean mailSmtpAuthNtlmDisable;
    private final String mailSmtpAuthNtlmDomain;
    private final Integer mailSmtpAuthNtlmFlags;
    private final Boolean mailSmtpAuthXoauth2Disable;
    private final String mailSmtpSubmitter;
    private String mailSmtpDnsNotify;
    private String mailSmtpDnsRet;
    private Boolean mailSmtpAllow8bitmime;
    private Boolean mailSmtpSendPartial;
    private final Boolean mailSmtpSaslEnable;
    private final String mailSmtpSaslMechanisms;
    private final String mailSmtpSaslAuthorizationId;
    private final String mailSmtpSaslRealm;
    private final Boolean mailSmtpSaslUseCanonicalHostname;
    private final Boolean mailSmtpQuitwait;
    private final Boolean mailSmtpReportSuccess;
    private final Boolean mailSmtpSslEnable;
    private final Boolean mailSmtpSslCheckServerIdentity;
    private final String mailSmtpSslTrust;
    private final String mailSmtpSslProtocols;
    private final String mailSmtpSslCipherSuites;
    private final Boolean mailSmtpStartTlsEnable;
    private final Boolean mailSmtpStartTlsRequired;
    private final String mailSmtpProxyHost;
    private final Integer mailSmtpProxyPort;
    private final String mailSmtpSocksHost;
    private final Integer mailSmtpSocksPort;
    private final String mailSmtpMailExtension;
    private final Boolean mailSmtpUserSet;
    private final Boolean mailSmtpNoopStrict;
    private Long id;

    public MockEmailGlobalEntity() {
        this("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", false, 99, 400, 500, 600, "MailSmtpFrom", "MailSmtpLocalhost", "MailSmtpLocalAddress", 700, true, false, "MailSmtpAuthMechanisms", true, false, true,
                false, "MailSmtpAuthNtlmDomain", 22, false, "MailSmtpSubmitter", "MailSmtpDnsNotify", "MailSmtpDnsRet", true, false, true, "MailSmtpSaslMechanisms", "MailSmtpSaslAuthorizationId", "MailSmtpSaslRealm",
                true, false, true, false, true, "MailSmtpSslTrust", "MailSmtpSslProtocols", "MailSmtpSslCipherSuites", true, false, "MailSmtpProxyHost", 800, "MailSmtpSocksHost", 900, "MailSmtpMailExtension",
                true, false, 1L);
    }

    private MockEmailGlobalEntity(final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final boolean mailSmtpPasswordIsSet, final Integer mailSmtpPort, final Integer mailSmtpConnectionTimeout,
            final Integer mailSmtpTimeout,
            final Integer mailSmtpWriteTimeout, final String mailSmtpFrom, final String mailSmtpLocalhost, final String mailSmtpLocalAddress, final Integer mailSmtpLocalPort, final Boolean mailSmtpEhlo, final Boolean mailSmtpAuth,
            final String mailSmtpAuthMechanisms, final Boolean mailSmtpAuthLoginDisable, final Boolean mailSmtpAuthPlainDisable, final Boolean mailSmtpAuthDigestMd5Disable, final Boolean mailSmtpAuthNtlmDisable,
            final String mailSmtpAuthNtlmDomain, final Integer mailSmtpAuthNtlmFlags, final Boolean mailSmtpAuthXoauth2Disable, final String mailSmtpSubmitter, final String mailSmtpDnsNotify, final String mailSmtpDnsRet,
            final Boolean mailSmtpAllow8bitmime, final Boolean mailSmtpSendPartial, final Boolean mailSmtpSaslEnable, final String mailSmtpSaslMechanisms, final String mailSmtpSaslAuthorizationId, final String mailSmtpSaslRealm,
            final Boolean mailSmtpSaslUseCanonicalHostname, final Boolean mailSmtpQuitwait, final Boolean mailSmtpReportSuccess, final Boolean mailSmtpSslEnable, final Boolean mailSmtpSslCheckServerIdentity, final String mailSmtpSslTrust,
            final String mailSmtpSslProtocols, final String mailSmtpSslCipherSuites, final Boolean mailSmtpStartTlsEnable, final Boolean mailSmtpStartTlsRequired, final String mailSmtpProxyHost, final Integer mailSmtpProxyPort,
            final String mailSmtpSocksHost, final Integer mailSmtpSocksPort, final String mailSmtpMailExtension, final Boolean mailSmtpUserSet, final Boolean mailSmtpNoopStrict, final Long id) {
        super();
        this.mailSmtpHost = mailSmtpHost;
        this.mailSmtpUser = mailSmtpUser;
        this.mailSmtpPassword = mailSmtpPassword;
        this.mailSmtpPasswordIsSet = mailSmtpPasswordIsSet;
        this.mailSmtpPort = mailSmtpPort;
        this.mailSmtpConnectionTimeout = mailSmtpConnectionTimeout;
        this.mailSmtpTimeout = mailSmtpTimeout;
        this.mailSmtpWriteTimeout = mailSmtpWriteTimeout;
        this.mailSmtpFrom = mailSmtpFrom;
        this.mailSmtpLocalhost = mailSmtpLocalhost;
        this.mailSmtpLocalAddress = mailSmtpLocalAddress;
        this.mailSmtpLocalPort = mailSmtpLocalPort;
        this.mailSmtpEhlo = mailSmtpEhlo;
        this.mailSmtpAuth = mailSmtpAuth;
        this.mailSmtpAuthMechanisms = mailSmtpAuthMechanisms;
        this.mailSmtpAuthLoginDisable = mailSmtpAuthLoginDisable;
        this.mailSmtpAuthPlainDisable = mailSmtpAuthPlainDisable;
        this.mailSmtpAuthDigestMd5Disable = mailSmtpAuthDigestMd5Disable;
        this.mailSmtpAuthNtlmDisable = mailSmtpAuthNtlmDisable;
        this.mailSmtpAuthNtlmDomain = mailSmtpAuthNtlmDomain;
        this.mailSmtpAuthNtlmFlags = mailSmtpAuthNtlmFlags;
        this.mailSmtpAuthXoauth2Disable = mailSmtpAuthXoauth2Disable;
        this.mailSmtpSubmitter = mailSmtpSubmitter;
        this.mailSmtpDnsNotify = mailSmtpDnsNotify;
        this.mailSmtpDnsRet = mailSmtpDnsRet;
        this.mailSmtpAllow8bitmime = mailSmtpAllow8bitmime;
        this.mailSmtpSendPartial = mailSmtpSendPartial;
        this.mailSmtpSaslEnable = mailSmtpSaslEnable;
        this.mailSmtpSaslMechanisms = mailSmtpSaslMechanisms;
        this.mailSmtpSaslAuthorizationId = mailSmtpSaslAuthorizationId;
        this.mailSmtpSaslRealm = mailSmtpSaslRealm;
        this.mailSmtpSaslUseCanonicalHostname = mailSmtpSaslUseCanonicalHostname;
        this.mailSmtpQuitwait = mailSmtpQuitwait;
        this.mailSmtpReportSuccess = mailSmtpReportSuccess;
        this.mailSmtpSslEnable = mailSmtpSslEnable;
        this.mailSmtpSslCheckServerIdentity = mailSmtpSslCheckServerIdentity;
        this.mailSmtpSslTrust = mailSmtpSslTrust;
        this.mailSmtpSslProtocols = mailSmtpSslProtocols;
        this.mailSmtpSslCipherSuites = mailSmtpSslCipherSuites;
        this.mailSmtpStartTlsEnable = mailSmtpStartTlsEnable;
        this.mailSmtpStartTlsRequired = mailSmtpStartTlsRequired;
        this.mailSmtpProxyHost = mailSmtpProxyHost;
        this.mailSmtpProxyPort = mailSmtpProxyPort;
        this.mailSmtpSocksHost = mailSmtpSocksHost;
        this.mailSmtpSocksPort = mailSmtpSocksPort;
        this.mailSmtpMailExtension = mailSmtpMailExtension;
        this.mailSmtpUserSet = mailSmtpUserSet;
        this.mailSmtpNoopStrict = mailSmtpNoopStrict;
        this.id = id;
    }

    public String getMailSmtpHost() {
        return mailSmtpHost;
    }

    public void setMailSmtpHost(final String mailSmtpHost) {
        this.mailSmtpHost = mailSmtpHost;
    }

    public String getMailSmtpUser() {
        return mailSmtpUser;
    }

    public void setMailSmtpUser(final String mailSmtpUser) {
        this.mailSmtpUser = mailSmtpUser;
    }

    public String getMailSmtpPassword() {
        return mailSmtpPassword;
    }

    public void setMailSmtpPassword(final String mailSmtpPassword) {
        this.mailSmtpPassword = mailSmtpPassword;
    }

    public boolean isMailSmtpPasswordIsSet() {
        return mailSmtpPasswordIsSet;
    }

    public void setMailSmtpPasswordIsSet(final boolean mailSmtpPasswordIsSet) {
        this.mailSmtpPasswordIsSet = mailSmtpPasswordIsSet;
    }

    public Integer getMailSmtpPort() {
        return mailSmtpPort;
    }

    public void setMailSmtpPort(final Integer mailSmtpPort) {
        this.mailSmtpPort = mailSmtpPort;
    }

    public Integer getMailSmtpConnectionTimeout() {
        return mailSmtpConnectionTimeout;
    }

    public void setMailSmtpConnectionTimeout(final Integer mailSmtpConnectionTimeout) {
        this.mailSmtpConnectionTimeout = mailSmtpConnectionTimeout;
    }

    public Integer getMailSmtpTimeout() {
        return mailSmtpTimeout;
    }

    public void setMailSmtpTimeout(final Integer mailSmtpTimeout) {
        this.mailSmtpTimeout = mailSmtpTimeout;
    }

    public String getMailSmtpFrom() {
        return mailSmtpFrom;
    }

    public void setMailSmtpFrom(final String mailSmtpFrom) {
        this.mailSmtpFrom = mailSmtpFrom;
    }

    public String getMailSmtpLocalhost() {
        return mailSmtpLocalhost;
    }

    public void setMailSmtpLocalhost(final String mailSmtpLocalhost) {
        this.mailSmtpLocalhost = mailSmtpLocalhost;
    }

    public Boolean getMailSmtpEhlo() {
        return mailSmtpEhlo;
    }

    public void setMailSmtpEhlo(final Boolean mailSmtpEhlo) {
        this.mailSmtpEhlo = mailSmtpEhlo;
    }

    public Boolean getMailSmtpAuth() {
        return mailSmtpAuth;
    }

    public void setMailSmtpAuth(final Boolean mailSmtpAuth) {
        this.mailSmtpAuth = mailSmtpAuth;
    }

    public String getMailSmtpDnsNotify() {
        return mailSmtpDnsNotify;
    }

    public void setMailSmtpDnsNotify(final String mailSmtpDnsNotify) {
        this.mailSmtpDnsNotify = mailSmtpDnsNotify;
    }

    public String getMailSmtpDnsRet() {
        return mailSmtpDnsRet;
    }

    public void setMailSmtpDnsRet(final String mailSmtpDnsRet) {
        this.mailSmtpDnsRet = mailSmtpDnsRet;
    }

    public Boolean getMailSmtpAllow8bitmime() {
        return mailSmtpAllow8bitmime;
    }

    public void setMailSmtpAllow8bitmime(final Boolean mailSmtpAllow8bitmime) {
        this.mailSmtpAllow8bitmime = mailSmtpAllow8bitmime;
    }

    public Boolean getMailSmtpSendPartial() {
        return mailSmtpSendPartial;
    }

    public void setMailSmtpSendPartial(final Boolean mailSmtpSendPartial) {
        this.mailSmtpSendPartial = mailSmtpSendPartial;
    }

    public Integer getMailSmtpWriteTimeout() {
        return mailSmtpWriteTimeout;
    }

    public String getMailSmtpLocalAddress() {
        return mailSmtpLocalAddress;
    }

    public Integer getMailSmtpLocalPort() {
        return mailSmtpLocalPort;
    }

    public String getMailSmtpAuthMechanisms() {
        return mailSmtpAuthMechanisms;
    }

    public Boolean getMailSmtpAuthLoginDisable() {
        return mailSmtpAuthLoginDisable;
    }

    public Boolean getMailSmtpAuthPlainDisable() {
        return mailSmtpAuthPlainDisable;
    }

    public Boolean getMailSmtpAuthDigestMd5Disable() {
        return mailSmtpAuthDigestMd5Disable;
    }

    public Boolean getMailSmtpAuthNtlmDisable() {
        return mailSmtpAuthNtlmDisable;
    }

    public String getMailSmtpAuthNtlmDomain() {
        return mailSmtpAuthNtlmDomain;
    }

    public Integer getMailSmtpAuthNtlmFlags() {
        return mailSmtpAuthNtlmFlags;
    }

    public Boolean getMailSmtpAuthXoauth2Disable() {
        return mailSmtpAuthXoauth2Disable;
    }

    public String getMailSmtpSubmitter() {
        return mailSmtpSubmitter;
    }

    public Boolean getMailSmtpSaslEnable() {
        return mailSmtpSaslEnable;
    }

    public String getMailSmtpSaslMechanisms() {
        return mailSmtpSaslMechanisms;
    }

    public String getMailSmtpSaslAuthorizationId() {
        return mailSmtpSaslAuthorizationId;
    }

    public String getMailSmtpSaslRealm() {
        return mailSmtpSaslRealm;
    }

    public Boolean getMailSmtpSaslUseCanonicalHostname() {
        return mailSmtpSaslUseCanonicalHostname;
    }

    public Boolean getMailSmtpQuitwait() {
        return mailSmtpQuitwait;
    }

    public Boolean getMailSmtpReportSuccess() {
        return mailSmtpReportSuccess;
    }

    public Boolean getMailSmtpSslEnable() {
        return mailSmtpSslEnable;
    }

    public Boolean getMailSmtpSslCheckServerIdentity() {
        return mailSmtpSslCheckServerIdentity;
    }

    public String getMailSmtpSslTrust() {
        return mailSmtpSslTrust;
    }

    public String getMailSmtpSslProtocols() {
        return mailSmtpSslProtocols;
    }

    public String getMailSmtpSslCipherSuites() {
        return mailSmtpSslCipherSuites;
    }

    public Boolean getMailSmtpStartTlsEnable() {
        return mailSmtpStartTlsEnable;
    }

    public Boolean getMailSmtpStartTlsRequired() {
        return mailSmtpStartTlsRequired;
    }

    public String getMailSmtpProxyHost() {
        return mailSmtpProxyHost;
    }

    public Integer getMailSmtpProxyPort() {
        return mailSmtpProxyPort;
    }

    public String getMailSmtpSocksHost() {
        return mailSmtpSocksHost;
    }

    public Integer getMailSmtpSocksPort() {
        return mailSmtpSocksPort;
    }

    public String getMailSmtpMailExtension() {
        return mailSmtpMailExtension;
    }

    public Boolean getMailSmtpUserSet() {
        return mailSmtpUserSet;
    }

    public Boolean getMailSmtpNoopStrict() {
        return mailSmtpNoopStrict;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public EmailGlobalConfigEntity createGlobalEntity() {
        final EmailGlobalConfigEntity entity = new EmailGlobalConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout,
                mailSmtpWriteTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpLocalAddress, mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuth,
                mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable,
                mailSmtpAuthNtlmDomain, mailSmtpAuthNtlmFlags, mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet,
                mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId, mailSmtpSaslRealm,
                mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust,
                mailSmtpSslProtocols, mailSmtpSslCipherSuites, mailSmtpStartTlsEnable, mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort,
                mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict);
        entity.setId(id);
        return entity;
    }

    @Override
    public EmailGlobalConfigEntity createEmptyGlobalEntity() {
        return new EmailGlobalConfigEntity();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("mailSmtpHost", mailSmtpHost);
        json.addProperty("mailSmtpUser", mailSmtpUser);
        json.addProperty("mailSmtpPort", mailSmtpPort);
        json.addProperty("mailSmtpConnectionTimeout", mailSmtpConnectionTimeout);
        json.addProperty("mailSmtpTimeout", mailSmtpTimeout);
        json.addProperty("mailSmtpWriteTimeout", mailSmtpWriteTimeout);
        json.addProperty("mailSmtpFrom", mailSmtpFrom);
        json.addProperty("mailSmtpLocalhost", mailSmtpLocalhost);
        json.addProperty("mailSmtpLocalAddress", mailSmtpLocalAddress);
        json.addProperty("mailSmtpLocalPort", mailSmtpLocalPort);
        json.addProperty("mailSmtpEhlo", mailSmtpEhlo);
        json.addProperty("mailSmtpAuth", mailSmtpAuth);
        json.addProperty("mailSmtpAuthMechanisms", mailSmtpAuthMechanisms);
        json.addProperty("mailSmtpAuthLoginDisable", mailSmtpAuthLoginDisable);
        json.addProperty("mailSmtpAuthPlainDisable", mailSmtpAuthPlainDisable);
        json.addProperty("mailSmtpAuthDigestMd5Disable", mailSmtpAuthDigestMd5Disable);
        json.addProperty("mailSmtpAuthNtlmDisable", mailSmtpAuthNtlmDisable);
        json.addProperty("mailSmtpAuthNtlmDomain", mailSmtpAuthNtlmDomain);
        json.addProperty("mailSmtpAuthNtlmFlags", mailSmtpAuthNtlmFlags);
        json.addProperty("mailSmtpAuthXoauth2Disable", mailSmtpAuthXoauth2Disable);
        json.addProperty("mailSmtpSubmitter", mailSmtpSubmitter);
        json.addProperty("mailSmtpDnsNotify", mailSmtpDnsNotify);
        json.addProperty("mailSmtpDnsRet", mailSmtpDnsRet);
        json.addProperty("mailSmtpAllow8bitmime", mailSmtpAllow8bitmime);
        json.addProperty("mailSmtpSendPartial", mailSmtpSendPartial);
        json.addProperty("mailSmtpSaslEnable", mailSmtpSaslEnable);
        json.addProperty("mailSmtpSaslMechanisms", mailSmtpSaslMechanisms);
        json.addProperty("mailSmtpSaslAuthorizationId", mailSmtpSaslAuthorizationId);
        json.addProperty("mailSmtpSaslRealm", mailSmtpSaslRealm);
        json.addProperty("mailSmtpSaslUseCanonicalHostname", mailSmtpSaslUseCanonicalHostname);
        json.addProperty("mailSmtpQuitwait", mailSmtpQuitwait);
        json.addProperty("mailSmtpReportSuccess", mailSmtpReportSuccess);
        json.addProperty("mailSmtpSslEnable", mailSmtpSslEnable);
        json.addProperty("mailSmtpSslCheckServerIdentity", mailSmtpSslCheckServerIdentity);
        json.addProperty("mailSmtpSslTrust", mailSmtpSslTrust);
        json.addProperty("mailSmtpSslProtocols", mailSmtpSslProtocols);
        json.addProperty("mailSmtpSslCipherSuites", mailSmtpSslCipherSuites);
        json.addProperty("mailSmtpStartTlsEnable", mailSmtpStartTlsEnable);
        json.addProperty("mailSmtpStartTlsRequired", mailSmtpStartTlsRequired);
        json.addProperty("mailSmtpProxyHost", mailSmtpProxyHost);
        json.addProperty("mailSmtpProxyPort", mailSmtpProxyPort);
        json.addProperty("mailSmtpSocksHost", mailSmtpSocksHost);
        json.addProperty("mailSmtpSocksPort", mailSmtpSocksPort);
        json.addProperty("mailSmtpMailExtension", mailSmtpMailExtension);
        json.addProperty("mailSmtpUserSet", mailSmtpUserSet);
        json.addProperty("mailSmtpNoopStrict", mailSmtpNoopStrict);
        json.addProperty("id", id);
        return json.toString();
    }

}
