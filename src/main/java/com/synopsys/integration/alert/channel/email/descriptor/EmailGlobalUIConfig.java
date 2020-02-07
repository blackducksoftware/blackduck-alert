/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;

@Component
public class EmailGlobalUIConfig extends UIConfig {
    private static final String LABEL_SMTP_HOST = "Smtp Host";
    private static final String LABEL_SMTP_FROM = "Smtp From";
    private static final String LABEL_SMTP_AUTH = "Smtp Auth";
    private static final String LABEL_SMTP_USER = "Smtp User";
    private static final String LABEL_SMTP_PASSWORD = "Smtp Password";
    private static final String LABEL_SMTP_PORT = "Smtp Port";
    private static final String LABEL_SMTP_CONNECTION_TIMEOUT = "Smtp Connection Timeout";
    private static final String LABEL_SMTP_TIMEOUT = "Smtp Timeout";
    private static final String LABEL_SMTP_WRITE_TIMEOUT = "Smtp Write Timeout";
    private static final String LABEL_SMTP_LOCALHOST = "Smtp Localhost";
    private static final String LABEL_SMTP_LOCAL_ADDRESS = "Smtp Local Address";
    private static final String LABEL_SMTP_LOCAL_PORT = "Smtp Local Port";
    private static final String LABEL_SMTP_EHLO = "Smtp Ehlo";
    private static final String LABEL_SMTP_AUTH_MECHANISMS = "Smtp Auth Mechanisms";
    private static final String LABEL_SMTP_AUTH_LOGIN_DISABLE = "Smtp Auth Login Disable";
    private static final String LABEL_SMTP_AUTH_PLAIN_DISABLE = "Smtp Auth Plain Disable";
    private static final String LABEL_SMTP_AUTH_DIGEST_MD5_DISABLE = "Smtp Auth Digest MD5 Disable";
    private static final String LABEL_SMTP_AUTH_NTLM_DISABLE = "Smtp Auth Ntlm Disable";
    private static final String LABEL_SMTP_AUTH_NTLM_DOMAIN = "Smtp Auth Ntlm Domain";
    private static final String LABEL_SMTP_AUTH_NTLM_FLAGS = "Smtp Auth Ntlm Flags";
    private static final String LABEL_SMTP_AUTH_XOAUTH2_DISABLE = "SMTP Auth XOAuth2 Disable";
    private static final String LABEL_SMTP_SUBMITTER = "Smtp Submitter";
    private static final String LABEL_SMTP_DSN_NOTIFY = "Smtp DSN Notify";
    private static final String LABEL_SMTP_DSN_RET = "Smtp DSN Ret";
    private static final String LABEL_SMTP_ALLOW_8_BIT_MIME = "Smtp Allow 8-bit Mime";
    private static final String LABEL_SMTP_SEND_PARTIAL = "Smtp Send Partial";
    private static final String LABEL_SMTP_SASL_ENABLE = "Smtp SASL Enable";
    private static final String LABEL_SMTP_SASL_MECHANISMS = "Smtp SASL Mechanisms";
    private static final String LABEL_SMTP_SASL_AUTHORIZATION_ID = "Smtp SASL Authorization ID";
    private static final String LABEL_SMTP_SASL_REALM = "Smtp SASL Realm";
    private static final String LABEL_SMTP_SASL_USE_CANONICAL_HOSTNAME = "Smtp SASL Use Canonical Hostname";
    private static final String LABEL_SMTP_QUIT_WAIT = "Smtp Quit Wait";
    private static final String LABEL_SMTP_REPORT_SUCCESS = "Smtp Report Success";
    private static final String LABEL_SMTP_SSL_ENABLE = "Smtp SSL Enable";
    private static final String LABEL_SMTP_SSL_CHECK_SERVER_IDENTITY = "Smtp SSL Check Server Identity";
    private static final String LABEL_SMTP_SSL_TRUST = "Smtp SSL Trust";
    private static final String LABEL_SMTP_SSL_PROTOCOLS = "Smtp SSL Protocols";
    private static final String LABEL_SMTP_SSL_CIPHER_SUITES = "Smtp SSL Cipher Suites";
    private static final String LABEL_SMTP_START_TLS_ENABLED = "Smtp Start TLS Enabled";
    private static final String LABEL_SMTP_START_TLS_REQUIRED = "Smtp Start TLS Required";
    private static final String LABEL_SMTP_PROXY_HOST = "Smtp Proxy Host";
    private static final String LABEL_SMTP_PROXY_PORT = "Smtp Proxy Port";
    private static final String LABEL_SMTP_SOCKS_HOST = "Smtp Socks Host";
    private static final String LABEL_SMTP_SOCKS_PORT = "Smtp Socks Port";
    private static final String LABEL_SMTP_MAIL_EXTENSION = "Smtp Mail Extension";
    private static final String LABEL_SMTP_USE_RSET = "Smtp Use RSET";
    private static final String LABEL_SMTP_NOOP_STRICT = "Smtp NoOp Strict";

    private static final String JAVAMAIL_HOST_DESCRIPTION = "The host name of the SMTP email server.";
    private static final String JAVAMAIL_FROM_DESCRIPTION = "The email address to use as the return address.";
    private static final String JAVAMAIL_AUTH_DESCRIPTION = "Select this if your SMTP server requires authentication, then fill in the SMTP User and SMTP Password.";
    private static final String JAVAMAIL_USER_DESCRIPTION = "The username to authenticate with the SMTP server.";
    private static final String JAVAMAIL_PASSWORD_DESCRIPTION = "The password to authenticate with the SMTP server.";
    private static final String JAVAMAIL_PORT_DESCRIPTION = "The SMTP server port to connect to.";
    private static final String JAVAMAIL_CONNECTION_TIMEOUT_DESCRIPTION = "Socket connection timeout value in milliseconds.";
    private static final String JAVAMAIL_TIMEOUT_DESCRIPTION = "Socket read timeout value in milliseconds.";
    private static final String JAVAMAIL_WRITETIMEOUT_DESCRIPTION = "Socket write timeout value in milliseconds.";
    private static final String JAVAMAIL_LOCALHOST_DESCRIPTION = "Local host name used in the SMTP HELO or EHLO command.";
    private static final String JAVAMAIL_LOCALHOST_ADDRESS_DESCRIPTION = "Local address (host name) to bind to when creating the SMTP socket.";
    private static final String JAVAMAIL_LOCALHOST_PORT_DESCRIPTION = "Local port number to bind to when creating the SMTP socket.";
    private static final String JAVAMAIL_EHLO_DESCRIPTION = "If false, do not attempt to sign on with the EHLO command.";
    private static final String JAVAMAIL_AUTH_MECHANISMS_DESCRIPTION = "If set, lists the authentication mechanisms to consider, and the order in which to consider them. Only mechanisms supported by the server and supported by the current implementation will be used.";
    private static final String JAVAMAIL_AUTH_LOGIN_DISABLE_DESCRIPTION = "If true, prevents use of the AUTH LOGIN command.";
    private static final String JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_DESCRIPTION = "If true, prevents use of the AUTH PLAIN command.";
    private static final String JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_DESCRIPTION = "If true, prevents use of the AUTH DIGEST-MD5 command.";
    private static final String JAVAMAIL_AUTH_NTLM_DISABLE_DESCRIPTION = "If true, prevents use of the AUTH NTLM command.";
    private static final String JAVAMAIL_AUTH_NTLM_DOMAIN_DESCRIPTION = "The NTLM authentication domain.";
    private static final String JAVAMAIL_AUTH_NTLM_FLAGS_DESCRIPTION = "NTLM protocol-specific flags.";
    private static final String JAVAMAIL_AUTH_XOAUTH2_DISABLE_DESCRIPTION = "If true, prevents use of the AUTHENTICATE XOAUTH2 command.";
    private static final String JAVAMAIL_SUBMITTER_DESCRIPTION = "The submitter to use in the AUTH tag in the MAIL FROM command. Typically used by a mail relay to pass along information about the original submitter of the message";
    private static final String JAVAMAIL_DSN_NOTIFY_DESCRIPTION = "The NOTIFY option to the RCPT command.";
    private static final String JAVAMAIL_DSN_RET_DESCRIPTION = "The RET option to the MAIL command.";
    private static final String JAVAMAIL_ALLOW_8_BITMIME_DESCRIPTION = "If set to true, and the server supports the 8BITMIME extension, text parts of messages that use the \"quoted-printable\" or \"base64\" encodings are converted to use \"8bit\" encoding";
    private static final String JAVAMAIL_SEND_PARTIAL_DESCRIPTION = "If set to true, and a message has some valid and some invalid addresses, send the message anyway, reporting the partial failure with a SendFailedException.";
    private static final String JAVAMAIL_SASL_ENABLE_DESCRIPTION = "If set to true, attempt to use the javax.security.sasl package to choose an authentication mechanism for login.";
    private static final String JAVAMAIL_SASL_MECHANISMS_DESCRIPTION = "A space or comma separated list of SASL mechanism names to try to use.";
    private static final String JAVAMAIL_SASL_AUTHORIZATION_ID_DESCRIPTION = "The authorization ID to use in the SASL authentication. If not set, the authentication ID (user name) is used.";
    private static final String JAVAMAIL_SASL_REALM_DESCRIPTION = "The realm to use with DIGEST-MD5 authentication.";
    private static final String JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_DESCRIPTION = "If set to true, the canonical host name returned by InetAddress.getCanonicalHostName is passed to the SASL mechanism, instead of the host name used to connect.";
    private static final String JAVAMAIL_QUITWAIT_DESCRIPTION = "If set to false, the QUIT command is sent and the connection is immediately closed.";
    private static final String JAVAMAIL_REPORT_SUCCESS_DESCRIPTION = "If set to true, causes the transport to include an SMTPAddressSucceededException for each address that is successful.";
    private static final String JAVAMAIL_SSL_ENABLE_DESCRIPTION = "If set to true, use SSL to connect and use the SSL port by default.";
    private static final String JAVAMAIL_SSL_CHECKSERVERIDENTITY_DESCRIPTION = "If set to true, check the server identity as specified by RFC 2595.";
    private static final String JAVAMAIL_SSL_TRUST_DESCRIPTION = "If set, and a socket factory hasnt been specified, enables use of a MailSSLSocketFactory. If set to \"*\", all hosts are trusted.";
    private static final String JAVAMAIL_SSL_PROTOCOLS_DESCRIPTION = "Specifies the SSL protocols that will be enabled for SSL connections.";
    private static final String JAVAMAIL_SSL_CIPHERSUITES_DESCRIPTION = "Specifies the SSL cipher suites that will be enabled for SSL connections.";
    private static final String JAVAMAIL_STARTTLS_ENABLE_DESCRIPTION = "If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login commands. If the server does not support STARTTLS, the connection continues without the use of TLS.";
    private static final String JAVAMAIL_STARTTLS_REQUIRED_DESCRIPTION = "If true, requires the use of the STARTTLS command. If the server doesnt support the STARTTLS command, or the command fails, the connect method will fail.";
    private static final String JAVAMAIL_PROXY_HOST_DESCRIPTION = "Specifies the host name of an HTTP web proxy server that will be used for connections to the mail server.";
    private static final String JAVAMAIL_PROXY_PORT_DESCRIPTION = "Specifies the port number for the HTTP web proxy server.";
    private static final String JAVAMAIL_SOCKS_HOST_DESCRIPTION = "Specifies the host name of a SOCKS5 proxy server that will be used for connections to the mail server.";
    private static final String JAVAMAIL_SOCKS_PORT_DESCRIPTION = "Specifies the port number for the SOCKS5 proxy server.";
    private static final String JAVAMAIL_MAILEXTENSION_DESCRIPTION = "Extension string to append to the MAIL command. The extension string can be used to specify standard SMTP service extensions as well as vendor-specific extensions.";
    private static final String JAVAMAIL_USERSET_DESCRIPTION = "If set to true, use the RSET command instead of the NOOP command in the isConnected method.";
    private static final String JAVAMAIL_NOOP_STRICT_DESCRIPTION = "If set to true, insist on a 250 response code from the NOOP command to indicate success.";

    private static final String ADVANCED_PANEL = "Advanced";

    private static final String TEST_LABEL_ADDRESS = "Email address";

    public EmailGlobalUIConfig() {
        super(EmailDescriptor.EMAIL_LABEL, EmailDescriptor.EMAIL_DESCRIPTION, EmailDescriptor.EMAIL_URL, EmailDescriptor.EMAIL_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        // Default fields
        final ConfigField mailSmtpHost = TextInputConfigField.createRequired(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), LABEL_SMTP_HOST, JAVAMAIL_HOST_DESCRIPTION);
        final ConfigField mailSmtpFrom = TextInputConfigField.createRequired(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), LABEL_SMTP_FROM, JAVAMAIL_FROM_DESCRIPTION);
        final ConfigField mailSmtpUser = TextInputConfigField.create(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), LABEL_SMTP_USER, JAVAMAIL_USER_DESCRIPTION);
        final ConfigField mailSmtpPassword = PasswordConfigField.create(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), LABEL_SMTP_PASSWORD, JAVAMAIL_PASSWORD_DESCRIPTION);
        final ConfigField mailSmtpAuth = CheckboxConfigField.create(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), LABEL_SMTP_AUTH, JAVAMAIL_AUTH_DESCRIPTION)
                                             .requireField(mailSmtpUser.getKey())
                                             .requireField(mailSmtpPassword.getKey());

        // Advanced fields
        final ConfigField mailSmtpPort = NumberConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), LABEL_SMTP_PORT, JAVAMAIL_PORT_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpConnectionTimeout = NumberConfigField
                                                          .createPanel(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), LABEL_SMTP_CONNECTION_TIMEOUT, JAVAMAIL_CONNECTION_TIMEOUT_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpTimeout = NumberConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), LABEL_SMTP_TIMEOUT, JAVAMAIL_TIMEOUT_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpWriteTimeout = NumberConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey(), LABEL_SMTP_WRITE_TIMEOUT, JAVAMAIL_WRITETIMEOUT_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpLocalhost = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY.getPropertyKey(), LABEL_SMTP_LOCALHOST, JAVAMAIL_LOCALHOST_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpLocalAddress = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY.getPropertyKey(), LABEL_SMTP_LOCAL_ADDRESS, JAVAMAIL_LOCALHOST_ADDRESS_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpLocalPort = NumberConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey(), LABEL_SMTP_LOCAL_PORT, JAVAMAIL_LOCALHOST_PORT_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpEhlo = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), LABEL_SMTP_EHLO, JAVAMAIL_EHLO_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpAuthMechanisms = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY.getPropertyKey(), LABEL_SMTP_AUTH_MECHANISMS, JAVAMAIL_AUTH_MECHANISMS_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpAuthLoginDisable = CheckboxConfigField
                                                         .createPanel(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_LOGIN_DISABLE, JAVAMAIL_AUTH_LOGIN_DISABLE_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpAuthPlainDisable = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_PLAIN_DISABLE, JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_DESCRIPTION,
            ADVANCED_PANEL);
        final ConfigField mailSmtpAuthDigestMd5Disable = CheckboxConfigField
                                                             .createPanel(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_DIGEST_MD5_DISABLE, JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_DESCRIPTION,
                                                                 ADVANCED_PANEL);
        final ConfigField mailSmtpAuthNtlmDisable = CheckboxConfigField
                                                        .createPanel(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_NTLM_DISABLE, JAVAMAIL_AUTH_NTLM_DISABLE_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpAuthNtlmDomain = TextInputConfigField
                                                       .createPanel(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY.getPropertyKey(), LABEL_SMTP_AUTH_NTLM_DOMAIN, JAVAMAIL_AUTH_NTLM_DOMAIN_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpAuthNtlmFlags = NumberConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey(), LABEL_SMTP_AUTH_NTLM_FLAGS, JAVAMAIL_AUTH_NTLM_FLAGS_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpAuthXoauth2Disable = CheckboxConfigField
                                                           .createPanel(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_XOAUTH2_DISABLE, JAVAMAIL_AUTH_XOAUTH2_DISABLE_DESCRIPTION,
                                                               ADVANCED_PANEL);
        final ConfigField mailSmtpSubmitter = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY.getPropertyKey(), LABEL_SMTP_SUBMITTER, JAVAMAIL_SUBMITTER_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpDnsNotify = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY.getPropertyKey(), LABEL_SMTP_DSN_NOTIFY, JAVAMAIL_DSN_NOTIFY_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpDnsRet = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY.getPropertyKey(), LABEL_SMTP_DSN_RET, JAVAMAIL_DSN_RET_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpAllow8bitmime = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY.getPropertyKey(), LABEL_SMTP_ALLOW_8_BIT_MIME, JAVAMAIL_ALLOW_8_BITMIME_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSendPartial = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY.getPropertyKey(), LABEL_SMTP_SEND_PARTIAL, JAVAMAIL_SEND_PARTIAL_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSaslEnable = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY.getPropertyKey(), LABEL_SMTP_SASL_ENABLE, JAVAMAIL_SASL_ENABLE_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSaslMechanisms = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY.getPropertyKey(), LABEL_SMTP_SASL_MECHANISMS, JAVAMAIL_SASL_MECHANISMS_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSaslAuthorizationId = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY.getPropertyKey(), LABEL_SMTP_SASL_AUTHORIZATION_ID, JAVAMAIL_SASL_AUTHORIZATION_ID_DESCRIPTION,
            ADVANCED_PANEL);
        final ConfigField mailSmtpSaslRealm = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey(), LABEL_SMTP_SASL_REALM, JAVAMAIL_SASL_REALM_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSaslUseCanonicalHostname = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY.getPropertyKey(), LABEL_SMTP_SASL_USE_CANONICAL_HOSTNAME,
            JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpQuitwait = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY.getPropertyKey(), LABEL_SMTP_QUIT_WAIT, JAVAMAIL_QUITWAIT_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpReportSuccess = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY.getPropertyKey(), LABEL_SMTP_REPORT_SUCCESS, JAVAMAIL_REPORT_SUCCESS_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSslEnable = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY.getPropertyKey(), LABEL_SMTP_SSL_ENABLE, JAVAMAIL_SSL_ENABLE_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSslCheckServerIdentity = CheckboxConfigField
                                                               .createPanel(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY.getPropertyKey(), LABEL_SMTP_SSL_CHECK_SERVER_IDENTITY, JAVAMAIL_SSL_CHECKSERVERIDENTITY_DESCRIPTION,
                                                                   ADVANCED_PANEL);
        final ConfigField mailSmtpSslTrust = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY.getPropertyKey(), LABEL_SMTP_SSL_TRUST, JAVAMAIL_SSL_TRUST_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSslProtocols = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY.getPropertyKey(), LABEL_SMTP_SSL_PROTOCOLS, JAVAMAIL_SSL_PROTOCOLS_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSslCipherSuites = TextInputConfigField
                                                        .createPanel(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY.getPropertyKey(), LABEL_SMTP_SSL_CIPHER_SUITES, JAVAMAIL_SSL_CIPHERSUITES_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpStartTlsEnable = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY.getPropertyKey(), LABEL_SMTP_START_TLS_ENABLED, JAVAMAIL_STARTTLS_ENABLE_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpStartTlsRequired = CheckboxConfigField
                                                         .createPanel(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY.getPropertyKey(), LABEL_SMTP_START_TLS_REQUIRED, JAVAMAIL_STARTTLS_REQUIRED_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpProxyHost = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY.getPropertyKey(), LABEL_SMTP_PROXY_HOST, JAVAMAIL_PROXY_HOST_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpProxyPort = NumberConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey(), LABEL_SMTP_PROXY_PORT, JAVAMAIL_PROXY_PORT_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSocksHost = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY.getPropertyKey(), LABEL_SMTP_SOCKS_HOST, JAVAMAIL_SOCKS_HOST_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpSocksPort = NumberConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey(), LABEL_SMTP_SOCKS_PORT, JAVAMAIL_SOCKS_PORT_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpMailExtension = TextInputConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY.getPropertyKey(), LABEL_SMTP_MAIL_EXTENSION, JAVAMAIL_MAILEXTENSION_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpUserSet = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_USERSET_KEY.getPropertyKey(), LABEL_SMTP_USE_RSET, JAVAMAIL_USERSET_DESCRIPTION, ADVANCED_PANEL);
        final ConfigField mailSmtpNoopStrict = CheckboxConfigField.createPanel(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY.getPropertyKey(), LABEL_SMTP_NOOP_STRICT, JAVAMAIL_NOOP_STRICT_DESCRIPTION, ADVANCED_PANEL);

        return List.of(mailSmtpHost, mailSmtpFrom, mailSmtpAuth, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpWriteTimeout, mailSmtpLocalhost, mailSmtpLocalAddress,
            mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable, mailSmtpAuthNtlmDomain, mailSmtpAuthNtlmFlags,
            mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId, mailSmtpSaslRealm,
            mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust, mailSmtpSslProtocols, mailSmtpSslCipherSuites, mailSmtpStartTlsEnable,
            mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort, mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict
        );
    }

    @Override
    public String createTestLabel() {
        return TEST_LABEL_ADDRESS;
    }
}