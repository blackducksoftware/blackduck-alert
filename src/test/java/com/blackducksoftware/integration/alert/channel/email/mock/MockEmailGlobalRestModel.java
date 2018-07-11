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
package com.blackducksoftware.integration.alert.channel.email.mock;

import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.alert.mock.model.global.MockGlobalRestModelUtil;
import com.google.gson.JsonObject;

public class MockEmailGlobalRestModel extends MockGlobalRestModelUtil<GlobalEmailConfigRestModel> {
    private String mailSmtpHost;
    private String mailSmtpUser;
    private String mailSmtpPassword;
    private boolean mailSmtpPasswordIsSet;
    private String mailSmtpPort;
    private String mailSmtpConnectionTimeout;
    private String mailSmtpTimeout;
    private String mailSmtpWriteTimeout;
    private String mailSmtpFrom;
    private String mailSmtpLocalhost;
    private String mailSmtpLocalAddress;
    private String mailSmtpLocalPort;
    private boolean mailSmtpEhlo;
    private boolean mailSmtpAuth;
    private String mailSmtpAuthMechanisms;
    private boolean mailSmtpAuthLoginDisable;
    private boolean mailSmtpAuthPlainDisable;
    private boolean mailSmtpAuthDigestMd5Disable;
    private boolean mailSmtpAuthNtlmDisable;
    private String mailSmtpAuthNtlmDomain;
    private String mailSmtpAuthNtlmFlags;
    private boolean mailSmtpAuthXoauth2Disable;
    private String mailSmtpSubmitter;
    private String mailSmtpDnsNotify;
    private String mailSmtpDnsRet;
    private boolean mailSmtpAllow8bitmime;
    private boolean mailSmtpSendPartial;
    private boolean mailSmtpSaslEnable;
    private String mailSmtpSaslMechanisms;
    private String mailSmtpSaslAuthorizationId;
    private String mailSmtpSaslRealm;
    private boolean mailSmtpSaslUseCanonicalHostname;
    private boolean mailSmtpQuitwait;
    private boolean mailSmtpReportSuccess;
    private boolean mailSmtpSslEnable;
    private boolean mailSmtpSslCheckServerIdentity;
    private String mailSmtpSslTrust;
    private String mailSmtpSslProtocols;
    private String mailSmtpSslCipherSuites;
    private boolean mailSmtpStartTlsEnable;
    private boolean mailSmtpStartTlsRequired;
    private String mailSmtpProxyHost;
    private String mailSmtpProxyPort;
    private String mailSmtpSocksHost;
    private String mailSmtpSocksPort;
    private String mailSmtpMailExtension;
    private boolean mailSmtpUserSet;
    private boolean mailSmtpNoopStrict;
    private String id;

    public MockEmailGlobalRestModel() {
        this("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", false, "99", "400", "500", "600", "MailSmtpFrom", "MailSmtpLocalhost", "MailSmtpLocalAddress", "700", true, false, "MailSmtpAuthMechanisms", true, false, true,
                false, "MailSmtpAuthNtlmDomain", "22", false, "MailSmtpSubmitter", "MailSmtpDnsNotify", "MailSmtpDnsRet", true, false, true, "MailSmtpSaslMechanisms", "MailSmtpSaslAuthorizationId", "MailSmtpSaslRealm",
                true, false, true, false, true, "MailSmtpSslTrust", "MailSmtpSslProtocols", "MailSmtpSslCipherSuites", true, false, "MailSmtpProxyHost", "800", "MailSmtpSocksHost", "900", "MailSmtpMailExtension",
                true, false, "1");
    }

    private MockEmailGlobalRestModel(final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final boolean mailSmtpPasswordIsSet, final String mailSmtpPort,
            final String mailSmtpConnectionTimeout, final String mailSmtpTimeout,
            final String mailSmtpWriteTimeout, final String mailSmtpFrom, final String mailSmtpLocalhost, final String mailSmtpLocalAddress, final String mailSmtpLocalPort, final boolean mailSmtpEhlo, final boolean mailSmtpAuth,
            final String mailSmtpAuthMechanisms, final boolean mailSmtpAuthLoginDisable, final boolean mailSmtpAuthPlainDisable, final boolean mailSmtpAuthDigestMd5Disable, final boolean mailSmtpAuthNtlmDisable,
            final String mailSmtpAuthNtlmDomain, final String mailSmtpAuthNtlmFlags, final boolean mailSmtpAuthXoauth2Disable, final String mailSmtpSubmitter, final String mailSmtpDnsNotify, final String mailSmtpDnsRet,
            final boolean mailSmtpAllow8bitmime, final boolean mailSmtpSendPartial, final boolean mailSmtpSaslEnable, final String mailSmtpSaslMechanisms, final String mailSmtpSaslAuthorizationId, final String mailSmtpSaslRealm,
            final boolean mailSmtpSaslUseCanonicalHostname, final boolean mailSmtpQuitwait, final boolean mailSmtpReportSuccess, final boolean mailSmtpSslEnable, final boolean mailSmtpSslCheckServerIdentity, final String mailSmtpSslTrust,
            final String mailSmtpSslProtocols, final String mailSmtpSslCipherSuites, final boolean mailSmtpStartTlsEnable, final boolean mailSmtpStartTlsRequired, final String mailSmtpProxyHost, final String mailSmtpProxyPort,
            final String mailSmtpSocksHost, final String mailSmtpSocksPort, final String mailSmtpMailExtension, final boolean mailSmtpUserSet, final boolean mailSmtpNoopStrict, final String id) {
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

    public String getMailSmtpPort() {
        return mailSmtpPort;
    }

    public void setMailSmtpPort(final String mailSmtpPort) {
        this.mailSmtpPort = mailSmtpPort;
    }

    public String getMailSmtpConnectionTimeout() {
        return mailSmtpConnectionTimeout;
    }

    public void setMailSmtpConnectionTimeout(final String mailSmtpConnectionTimeout) {
        this.mailSmtpConnectionTimeout = mailSmtpConnectionTimeout;
    }

    public String getMailSmtpTimeout() {
        return mailSmtpTimeout;
    }

    public void setMailSmtpTimeout(final String mailSmtpTimeout) {
        this.mailSmtpTimeout = mailSmtpTimeout;
    }

    public String getMailSmtpWriteTimeout() {
        return mailSmtpWriteTimeout;
    }

    public void setMailSmtpWriteTimeout(final String mailSmtpWriteTimeout) {
        this.mailSmtpWriteTimeout = mailSmtpWriteTimeout;
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

    public String getMailSmtpLocalAddress() {
        return mailSmtpLocalAddress;
    }

    public void setMailSmtpLocalAddress(final String mailSmtpLocalAddress) {
        this.mailSmtpLocalAddress = mailSmtpLocalAddress;
    }

    public String getMailSmtpLocalPort() {
        return mailSmtpLocalPort;
    }

    public void setMailSmtpLocalPort(final String mailSmtpLocalPort) {
        this.mailSmtpLocalPort = mailSmtpLocalPort;
    }

    public boolean getMailSmtpEhlo() {
        return mailSmtpEhlo;
    }

    public void setMailSmtpEhlo(final boolean mailSmtpEhlo) {
        this.mailSmtpEhlo = mailSmtpEhlo;
    }

    public boolean getMailSmtpAuth() {
        return mailSmtpAuth;
    }

    public void setMailSmtpAuth(final boolean mailSmtpAuth) {
        this.mailSmtpAuth = mailSmtpAuth;
    }

    public String getMailSmtpAuthMechanisms() {
        return mailSmtpAuthMechanisms;
    }

    public void setMailSmtpAuthMechanisms(final String mailSmtpAuthMechanisms) {
        this.mailSmtpAuthMechanisms = mailSmtpAuthMechanisms;
    }

    public boolean getMailSmtpAuthLoginDisable() {
        return mailSmtpAuthLoginDisable;
    }

    public void setMailSmtpAuthLoginDisable(final boolean mailSmtpAuthLoginDisable) {
        this.mailSmtpAuthLoginDisable = mailSmtpAuthLoginDisable;
    }

    public boolean getMailSmtpAuthPlainDisable() {
        return mailSmtpAuthPlainDisable;
    }

    public void setMailSmtpAuthPlainDisable(final boolean mailSmtpAuthPlainDisable) {
        this.mailSmtpAuthPlainDisable = mailSmtpAuthPlainDisable;
    }

    public boolean getMailSmtpAuthDigestMd5Disable() {
        return mailSmtpAuthDigestMd5Disable;
    }

    public void setMailSmtpAuthDigestMd5Disable(final boolean mailSmtpAuthDigestMd5Disable) {
        this.mailSmtpAuthDigestMd5Disable = mailSmtpAuthDigestMd5Disable;
    }

    public boolean getMailSmtpAuthNtlmDisable() {
        return mailSmtpAuthNtlmDisable;
    }

    public void setMailSmtpAuthNtlmDisable(final boolean mailSmtpAuthNtlmDisable) {
        this.mailSmtpAuthNtlmDisable = mailSmtpAuthNtlmDisable;
    }

    public String getMailSmtpAuthNtlmDomain() {
        return mailSmtpAuthNtlmDomain;
    }

    public void setMailSmtpAuthNtlmDomain(final String mailSmtpAuthNtlmDomain) {
        this.mailSmtpAuthNtlmDomain = mailSmtpAuthNtlmDomain;
    }

    public String getMailSmtpAuthNtlmFlags() {
        return mailSmtpAuthNtlmFlags;
    }

    public void setMailSmtpAuthNtlmFlags(final String mailSmtpAuthNtlmFlags) {
        this.mailSmtpAuthNtlmFlags = mailSmtpAuthNtlmFlags;
    }

    public boolean getMailSmtpAuthXoauth2Disable() {
        return mailSmtpAuthXoauth2Disable;
    }

    public void setMailSmtpAuthXoauth2Disable(final boolean mailSmtpAuthXoauth2Disable) {
        this.mailSmtpAuthXoauth2Disable = mailSmtpAuthXoauth2Disable;
    }

    public String getMailSmtpSubmitter() {
        return mailSmtpSubmitter;
    }

    public void setMailSmtpSubmitter(final String mailSmtpSubmitter) {
        this.mailSmtpSubmitter = mailSmtpSubmitter;
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

    public boolean getMailSmtpAllow8bitmime() {
        return mailSmtpAllow8bitmime;
    }

    public void setMailSmtpAllow8bitmime(final boolean mailSmtpAllow8bitmime) {
        this.mailSmtpAllow8bitmime = mailSmtpAllow8bitmime;
    }

    public boolean getMailSmtpSendPartial() {
        return mailSmtpSendPartial;
    }

    public void setMailSmtpSendPartial(final boolean mailSmtpSendPartial) {
        this.mailSmtpSendPartial = mailSmtpSendPartial;
    }

    public boolean getMailSmtpSaslEnable() {
        return mailSmtpSaslEnable;
    }

    public void setMailSmtpSaslEnable(final boolean mailSmtpSaslEnable) {
        this.mailSmtpSaslEnable = mailSmtpSaslEnable;
    }

    public String getMailSmtpSaslMechanisms() {
        return mailSmtpSaslMechanisms;
    }

    public void setMailSmtpSaslMechanisms(final String mailSmtpSaslMechanisms) {
        this.mailSmtpSaslMechanisms = mailSmtpSaslMechanisms;
    }

    public String getMailSmtpSaslAuthorizationId() {
        return mailSmtpSaslAuthorizationId;
    }

    public void setMailSmtpSaslAuthorizationId(final String mailSmtpSaslAuthorizationId) {
        this.mailSmtpSaslAuthorizationId = mailSmtpSaslAuthorizationId;
    }

    public String getMailSmtpSaslRealm() {
        return mailSmtpSaslRealm;
    }

    public void setMailSmtpSaslRealm(final String mailSmtpSaslRealm) {
        this.mailSmtpSaslRealm = mailSmtpSaslRealm;
    }

    public boolean getMailSmtpSaslUseCanonicalHostname() {
        return mailSmtpSaslUseCanonicalHostname;
    }

    public void setMailSmtpSaslUseCanonicalHostname(final boolean mailSmtpSaslUseCanonicalHostname) {
        this.mailSmtpSaslUseCanonicalHostname = mailSmtpSaslUseCanonicalHostname;
    }

    public boolean getMailSmtpQuitwait() {
        return mailSmtpQuitwait;
    }

    public void setMailSmtpQuitwait(final boolean mailSmtpQuitwait) {
        this.mailSmtpQuitwait = mailSmtpQuitwait;
    }

    public boolean getMailSmtpReportSuccess() {
        return mailSmtpReportSuccess;
    }

    public void setMailSmtpReportSuccess(final boolean mailSmtpReportSuccess) {
        this.mailSmtpReportSuccess = mailSmtpReportSuccess;
    }

    public boolean getMailSmtpSslEnable() {
        return mailSmtpSslEnable;
    }

    public void setMailSmtpSslEnable(final boolean mailSmtpSslEnable) {
        this.mailSmtpSslEnable = mailSmtpSslEnable;
    }

    public boolean getMailSmtpSslCheckServerIdentity() {
        return mailSmtpSslCheckServerIdentity;
    }

    public void setMailSmtpSslCheckServerIdentity(final boolean mailSmtpSslCheckServerIdentity) {
        this.mailSmtpSslCheckServerIdentity = mailSmtpSslCheckServerIdentity;
    }

    public String getMailSmtpSslTrust() {
        return mailSmtpSslTrust;
    }

    public void setMailSmtpSslTrust(final String mailSmtpSslTrust) {
        this.mailSmtpSslTrust = mailSmtpSslTrust;
    }

    public String getMailSmtpSslProtocols() {
        return mailSmtpSslProtocols;
    }

    public void setMailSmtpSslProtocols(final String mailSmtpSslProtocols) {
        this.mailSmtpSslProtocols = mailSmtpSslProtocols;
    }

    public String getMailSmtpSslCipherSuites() {
        return mailSmtpSslCipherSuites;
    }

    public void setMailSmtpSslCipherSuites(final String mailSmtpSslCipherSuites) {
        this.mailSmtpSslCipherSuites = mailSmtpSslCipherSuites;
    }

    public boolean getMailSmtpStartTlsEnable() {
        return mailSmtpStartTlsEnable;
    }

    public void setMailSmtpStartTlsEnable(final boolean mailSmtpStartTlsEnable) {
        this.mailSmtpStartTlsEnable = mailSmtpStartTlsEnable;
    }

    public boolean getMailSmtpStartTlsRequired() {
        return mailSmtpStartTlsRequired;
    }

    public void setMailSmtpStartTlsRequired(final boolean mailSmtpStartTlsRequired) {
        this.mailSmtpStartTlsRequired = mailSmtpStartTlsRequired;
    }

    public String getMailSmtpProxyHost() {
        return mailSmtpProxyHost;
    }

    public void setMailSmtpProxyHost(final String mailSmtpProxyHost) {
        this.mailSmtpProxyHost = mailSmtpProxyHost;
    }

    public String getMailSmtpProxyPort() {
        return mailSmtpProxyPort;
    }

    public void setMailSmtpProxyPort(final String mailSmtpProxyPort) {
        this.mailSmtpProxyPort = mailSmtpProxyPort;
    }

    public String getMailSmtpSocksHost() {
        return mailSmtpSocksHost;
    }

    public void setMailSmtpSocksHost(final String mailSmtpSocksHost) {
        this.mailSmtpSocksHost = mailSmtpSocksHost;
    }

    public String getMailSmtpSocksPort() {
        return mailSmtpSocksPort;
    }

    public void setMailSmtpSocksPort(final String mailSmtpSocksPort) {
        this.mailSmtpSocksPort = mailSmtpSocksPort;
    }

    public String getMailSmtpMailExtension() {
        return mailSmtpMailExtension;
    }

    public void setMailSmtpMailExtension(final String mailSmtpMailExtension) {
        this.mailSmtpMailExtension = mailSmtpMailExtension;
    }

    public boolean getMailSmtpUserSet() {
        return mailSmtpUserSet;
    }

    public void setMailSmtpUserSet(final boolean mailSmtpUserSet) {
        this.mailSmtpUserSet = mailSmtpUserSet;
    }

    public boolean getMailSmtpNoopStrict() {
        return mailSmtpNoopStrict;
    }

    public void setMailSmtpNoopStrict(final boolean mailSmtpNoopStrict) {
        this.mailSmtpNoopStrict = mailSmtpNoopStrict;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public GlobalEmailConfigRestModel createGlobalRestModel() {
        final GlobalEmailConfigRestModel restModel = new GlobalEmailConfigRestModel(id, mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPasswordIsSet, mailSmtpPort,
                mailSmtpConnectionTimeout, mailSmtpTimeout,
                mailSmtpWriteTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpLocalAddress, mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuth,
                mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable,
                mailSmtpAuthNtlmDomain, mailSmtpAuthNtlmFlags, mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet,
                mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId, mailSmtpSaslRealm,
                mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust,
                mailSmtpSslProtocols, mailSmtpSslCipherSuites, mailSmtpStartTlsEnable, mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort,
                mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict);
        return restModel;
    }

    @Override
    public GlobalEmailConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalEmailConfigRestModel();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("mailSmtpHost", mailSmtpHost);
        json.addProperty("mailSmtpUser", mailSmtpUser);
        json.addProperty("mailSmtpPassword", mailSmtpPassword);
        json.addProperty("mailSmtpPasswordIsSet", mailSmtpPasswordIsSet);
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

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("mailSmtpPasswordIsSet", false);
        json.addProperty("mailSmtpEhlo", false);
        json.addProperty("mailSmtpAuth", false);
        json.addProperty("mailSmtpAuthLoginDisable", false);
        json.addProperty("mailSmtpAuthPlainDisable", false);
        json.addProperty("mailSmtpAuthDigestMd5Disable", false);
        json.addProperty("mailSmtpAuthNtlmDisable", false);
        json.addProperty("mailSmtpAuthXoauth2Disable", false);
        json.addProperty("mailSmtpAllow8bitmime", false);
        json.addProperty("mailSmtpSendPartial", false);
        json.addProperty("mailSmtpSaslEnable", false);
        json.addProperty("mailSmtpSaslUseCanonicalHostname", false);
        json.addProperty("mailSmtpQuitwait", false);
        json.addProperty("mailSmtpReportSuccess", false);
        json.addProperty("mailSmtpSslEnable", false);
        json.addProperty("mailSmtpSslCheckServerIdentity", false);
        json.addProperty("mailSmtpStartTlsEnable", false);
        json.addProperty("mailSmtpStartTlsRequired", false);
        json.addProperty("mailSmtpUserSet", false);
        json.addProperty("mailSmtpNoopStrict", false);
        return json.toString();
    }

}
