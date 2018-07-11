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
package com.blackducksoftware.integration.alert.channel.email.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blackducksoftware.integration.alert.annotation.SensitiveField;
import com.blackducksoftware.integration.alert.datasource.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.web.security.StringEncryptionConverter;

@Entity
@Table(schema = "alert", name = "global_email_config")
public class GlobalEmailConfigEntity extends GlobalChannelConfigEntity {
    // JavaMail properties https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
    // Note: SocketFactory properties were omitted
    // If this class needs to be modified for new parameters the database structure needs to change to a new table that contains key value pairs as strings.

    @Column(name = "mail_smtp_host")
    private String mailSmtpHost;

    @Column(name = "mail_smtp_user")
    private String mailSmtpUser;

    // not a javamail property, but we are going to piggy-back to get the smtp password
    // @EncryptedStringField
    @Column(name = "mail_smtp_password")
    @SensitiveField
    @Convert(converter = StringEncryptionConverter.class)
    private String mailSmtpPassword;

    @Column(name = "mail_smtp_port")
    private Integer mailSmtpPort;

    @Column(name = "mail_smtp_connection_timeout")
    private Integer mailSmtpConnectionTimeout;

    @Column(name = "mail_smtp_timeout")
    private Integer mailSmtpTimeout;

    @Column(name = "mail_smtp_writetimeout")
    private Integer mailSmtpWriteTimeout;

    @Column(name = "mail_smtp_from")
    private String mailSmtpFrom;

    @Column(name = "mail_smtp_localhost")
    private String mailSmtpLocalhost;

    @Column(name = "mail_smtp_localaddress")
    private String mailSmtpLocalAddress;

    @Column(name = "mail_smtp_localport")
    private Integer mailSmtpLocalPort;

    @Column(name = "mail_smtp_ehlo")
    private Boolean mailSmtpEhlo;

    @Column(name = "mail_smtp_auth")
    private Boolean mailSmtpAuth;

    @Column(name = "mail_smtp_auth_mechanisms")
    private String mailSmtpAuthMechanisms;

    @Column(name = "mail_smtp_auth_login_disable")
    private Boolean mailSmtpAuthLoginDisable;

    @Column(name = "mail_smtp_auth_plain_disable")
    private Boolean mailSmtpAuthPlainDisable;

    @Column(name = "mail_smtp_auth_digest_md5_disable")
    private Boolean mailSmtpAuthDigestMd5Disable;

    @Column(name = "mail_smtp_auth_ntlm_disable")
    private Boolean mailSmtpAuthNtlmDisable;

    @Column(name = "mail_smtp_auth_ntlm_domain")
    private String mailSmtpAuthNtlmDomain;

    @Column(name = "mail_smtp_auth_ntlm_flags")
    private Integer mailSmtpAuthNtlmFlags;

    @Column(name = "mail_smtp_auth_xoauth2_disable")
    private Boolean mailSmtpAuthXoauth2Disable;

    @Column(name = "mail_smtp_submitter")
    private String mailSmtpSubmitter;

    @Column(name = "mail_smtp_dsn_notify")
    private String mailSmtpDnsNotify;

    @Column(name = "mail_smtp_dns_ret")
    private String mailSmtpDnsRet;

    @Column(name = "mail_smtp_allow_8_bitmime")
    private Boolean mailSmtpAllow8bitmime;

    @Column(name = "mail_smtp_send_partial")
    private Boolean mailSmtpSendPartial;

    @Column(name = "mail_smtp_sasl_enable")
    private Boolean mailSmtpSaslEnable;

    @Column(name = "mail_smtp_sasl_mechanisms")
    private String mailSmtpSaslMechanisms;

    @Column(name = "mail_smtp_sasl_authorizationid")
    private String mailSmtpSaslAuthorizationId;

    @Column(name = "mail_smtp_sasl_realm")
    private String mailSmtpSaslRealm;

    @Column(name = "mail_smtp_sasl_usecanonicalhostname")
    private Boolean mailSmtpSaslUseCanonicalHostname;

    @Column(name = "mail_smtp_quitwait")
    private Boolean mailSmtpQuitwait;

    @Column(name = "mail_smtp_reportsuccess")
    private Boolean mailSmtpReportSuccess;

    @Column(name = "mail_smtp_ssl_enable")
    private Boolean mailSmtpSslEnable;

    @Column(name = "mail_smtp_ssl_checkserveridentity")
    private Boolean mailSmtpSslCheckServerIdentity;

    @Column(name = "mail_smtp_ssl_trust")
    private String mailSmtpSslTrust;

    @Column(name = "mail_smtp_ssl_protocols")
    private String mailSmtpSslProtocols;

    @Column(name = "mail_smtp_ssl_ciphersuites")
    private String mailSmtpSslCipherSuites;

    @Column(name = "mail_smtp_starttls_enable")
    private Boolean mailSmtpStartTlsEnable;

    @Column(name = "mail_smtp_starttls_required")
    private Boolean mailSmtpStartTlsRequired;

    @Column(name = "mail_smtp_proxy_host")
    private String mailSmtpProxyHost;

    @Column(name = "mail_smtp_proxy_port")
    private Integer mailSmtpProxyPort;

    @Column(name = "mail_smtp_socks_host")
    private String mailSmtpSocksHost;

    @Column(name = "mail_smtp_socks_port")
    private Integer mailSmtpSocksPort;

    @Column(name = "mail_smtp_mailextension")
    private String mailSmtpMailExtension;

    @Column(name = "mail_smtp_userset")
    private Boolean mailSmtpUserSet;

    @Column(name = "mail_smtp_noop_strict")
    private Boolean mailSmtpNoopStrict;

    public GlobalEmailConfigEntity() {
    }

    public GlobalEmailConfigEntity(final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final Integer mailSmtpPort, final Integer mailSmtpConnectionTimeout, final Integer mailSmtpTimeout,
            final Integer mailSmtpWriteTimeout, final String mailSmtpFrom, final String mailSmtpLocalhost, final String mailSmtpLocalAddress, final Integer mailSmtpLocalPort, final Boolean mailSmtpEhlo, final Boolean mailSmtpAuth,
            final String mailSmtpAuthMechanisms, final Boolean mailSmtpAuthLoginDisable, final Boolean mailSmtpAuthPlainDisable, final Boolean mailSmtpAuthDigestMd5Disable, final Boolean mailSmtpAuthNtlmDisable,
            final String mailSmtpAuthNtlmDomain, final Integer mailSmtpAuthNtlmFlags, final Boolean mailSmtpAuthXoauth2Disable, final String mailSmtpSubmitter, final String mailSmtpDnsNotify, final String mailSmtpDnsRet,
            final Boolean mailSmtpAllow8bitmime, final Boolean mailSmtpSendPartial, final Boolean mailSmtpSaslEnable, final String mailSmtpSaslMechanisms, final String mailSmtpSaslAuthorizationId, final String mailSmtpSaslRealm,
            final Boolean mailSmtpSaslUseCanonicalHostname, final Boolean mailSmtpQuitwait, final Boolean mailSmtpReportSuccess, final Boolean mailSmtpSslEnable, final Boolean mailSmtpSslCheckServerIdentity, final String mailSmtpSslTrust,
            final String mailSmtpSslProtocols, final String mailSmtpSslCipherSuites, final Boolean mailSmtpStartTlsEnable, final Boolean mailSmtpStartTlsRequired, final String mailSmtpProxyHost, final Integer mailSmtpProxyPort,
            final String mailSmtpSocksHost, final Integer mailSmtpSocksPort, final String mailSmtpMailExtension, final Boolean mailSmtpUserSet, final Boolean mailSmtpNoopStrict) {
        this.mailSmtpHost = mailSmtpHost;
        this.mailSmtpUser = mailSmtpUser;
        this.mailSmtpPassword = mailSmtpPassword;
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

    public Integer getMailSmtpPort() {
        return mailSmtpPort;
    }

    public Integer getMailSmtpConnectionTimeout() {
        return mailSmtpConnectionTimeout;
    }

    public Integer getMailSmtpTimeout() {
        return mailSmtpTimeout;
    }

    public Integer getMailSmtpWriteTimeout() {
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

    public Integer getMailSmtpLocalPort() {
        return mailSmtpLocalPort;
    }

    public Boolean getMailSmtpEhlo() {
        return mailSmtpEhlo;
    }

    public Boolean getMailSmtpAuth() {
        return mailSmtpAuth;
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

    public String getMailSmtpDnsNotify() {
        return mailSmtpDnsNotify;
    }

    public String getMailSmtpDnsRet() {
        return mailSmtpDnsRet;
    }

    public Boolean getMailSmtpAllow8bitmime() {
        return mailSmtpAllow8bitmime;
    }

    public Boolean getMailSmtpSendPartial() {
        return mailSmtpSendPartial;
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
}
