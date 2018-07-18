/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.alert.web.channel.model;

import com.blackducksoftware.integration.alert.annotation.SensitiveField;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

public class GlobalEmailConfigRestModel extends ConfigRestModel {
    // JavaMail properties http://connector.sourceforge.net/doc-files/Properties.html

    private String mailSmtpHost;
    private String mailSmtpUser;
    // not a javamail property, but we are going to piggy-back to get the smtp password
    @SensitiveField
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

    public GlobalEmailConfigRestModel() {
    }

    public GlobalEmailConfigRestModel(final String id, final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final boolean mailSmtpPasswordIsSet, final String mailSmtpPort,
            final String mailSmtpConnectionTimeout, final String mailSmtpTimeout,
            final String mailSmtpWriteTimeout, final String mailSmtpFrom, final String mailSmtpLocalhost, final String mailSmtpLocalAddress, final String mailSmtpLocalPort, final boolean mailSmtpEhlo, final boolean mailSmtpAuth,
            final String mailSmtpAuthMechanisms, final boolean mailSmtpAuthLoginDisable, final boolean mailSmtpAuthPlainDisable, final boolean mailSmtpAuthDigestMd5Disable, final boolean mailSmtpAuthNtlmDisable,
            final String mailSmtpAuthNtlmDomain, final String mailSmtpAuthNtlmFlags, final boolean mailSmtpAuthXoauth2Disable, final String mailSmtpSubmitter, final String mailSmtpDnsNotify, final String mailSmtpDnsRet,
            final boolean mailSmtpAllow8bitmime, final boolean mailSmtpSendPartial, final boolean mailSmtpSaslEnable, final String mailSmtpSaslMechanisms, final String mailSmtpSaslAuthorizationId, final String mailSmtpSaslRealm,
            final boolean mailSmtpSaslUseCanonicalHostname, final boolean mailSmtpQuitwait, final boolean mailSmtpReportSuccess, final boolean mailSmtpSslEnable, final boolean mailSmtpSslCheckServerIdentity, final String mailSmtpSslTrust,
            final String mailSmtpSslProtocols, final String mailSmtpSslCipherSuites, final boolean mailSmtpStartTlsEnable, final boolean mailSmtpStartTlsRequired, final String mailSmtpProxyHost, final String mailSmtpProxyPort,
            final String mailSmtpSocksHost, final String mailSmtpSocksPort, final String mailSmtpMailExtension, final boolean mailSmtpUserSet, final boolean mailSmtpNoopStrict) {
        super(id);
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

    }

    public String getMailSmtpHost() {
        return mailSmtpHost;
    }

    public String getMailSmtpUser() {
        return mailSmtpUser;
    }

    public String getMailSmtpPassword() {
        return mailSmtpPassword;
    }

    public boolean isMailSmtpPasswordIsSet() {
        return mailSmtpPasswordIsSet;
    }

    public String getMailSmtpPort() {
        return mailSmtpPort;
    }

    public String getMailSmtpConnectionTimeout() {
        return mailSmtpConnectionTimeout;
    }

    public String getMailSmtpTimeout() {
        return mailSmtpTimeout;
    }

    public String getMailSmtpWriteTimeout() {
        return mailSmtpWriteTimeout;
    }

    public String getMailSmtpFrom() {
        return mailSmtpFrom;
    }

    public String getMailSmtpLocalhost() {
        return mailSmtpLocalhost;
    }

    public String getMailSmtpLocalAddress() {
        return mailSmtpLocalAddress;
    }

    public String getMailSmtpLocalPort() {
        return mailSmtpLocalPort;
    }

    public boolean getMailSmtpEhlo() {
        return mailSmtpEhlo;
    }

    public boolean getMailSmtpAuth() {
        return mailSmtpAuth;
    }

    public String getMailSmtpAuthMechanisms() {
        return mailSmtpAuthMechanisms;
    }

    public boolean getMailSmtpAuthLoginDisable() {
        return mailSmtpAuthLoginDisable;
    }

    public boolean getMailSmtpAuthPlainDisable() {
        return mailSmtpAuthPlainDisable;
    }

    public boolean getMailSmtpAuthDigestMd5Disable() {
        return mailSmtpAuthDigestMd5Disable;
    }

    public boolean getMailSmtpAuthNtlmDisable() {
        return mailSmtpAuthNtlmDisable;
    }

    public String getMailSmtpAuthNtlmDomain() {
        return mailSmtpAuthNtlmDomain;
    }

    public String getMailSmtpAuthNtlmFlags() {
        return mailSmtpAuthNtlmFlags;
    }

    public boolean getMailSmtpAuthXoauth2Disable() {
        return mailSmtpAuthXoauth2Disable;
    }

    public String getMailSmtpSubmitter() {
        return mailSmtpSubmitter;
    }

    public String getMailSmtpDnsNotify() {
        return mailSmtpDnsNotify;
    }

    public String getMailSmtpDnsRet() {
        return mailSmtpDnsRet;
    }

    public boolean getMailSmtpAllow8bitmime() {
        return mailSmtpAllow8bitmime;
    }

    public boolean getMailSmtpSendPartial() {
        return mailSmtpSendPartial;
    }

    public boolean getMailSmtpSaslEnable() {
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

    public boolean getMailSmtpSaslUseCanonicalHostname() {
        return mailSmtpSaslUseCanonicalHostname;
    }

    public boolean getMailSmtpQuitwait() {
        return mailSmtpQuitwait;
    }

    public boolean getMailSmtpReportSuccess() {
        return mailSmtpReportSuccess;
    }

    public boolean getMailSmtpSslEnable() {
        return mailSmtpSslEnable;
    }

    public boolean getMailSmtpSslCheckServerIdentity() {
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

    public boolean getMailSmtpStartTlsEnable() {
        return mailSmtpStartTlsEnable;
    }

    public boolean getMailSmtpStartTlsRequired() {
        return mailSmtpStartTlsRequired;
    }

    public String getMailSmtpProxyHost() {
        return mailSmtpProxyHost;
    }

    public String getMailSmtpProxyPort() {
        return mailSmtpProxyPort;
    }

    public String getMailSmtpSocksHost() {
        return mailSmtpSocksHost;
    }

    public String getMailSmtpSocksPort() {
        return mailSmtpSocksPort;
    }

    public String getMailSmtpMailExtension() {
        return mailSmtpMailExtension;
    }

    public boolean getMailSmtpUserSet() {
        return mailSmtpUserSet;
    }

    public boolean getMailSmtpNoopStrict() {
        return mailSmtpNoopStrict;
    }

}
