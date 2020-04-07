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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;

@Component
public class EmailGlobalUIConfig extends UIConfig {
    private static final String LABEL_SMTP_HOST = "SMTP Host";
    private static final String LABEL_SMTP_FROM = "SMTP From";
    private static final String LABEL_SMTP_AUTH = "SMTP Auth";
    private static final String LABEL_SMTP_USER = "SMTP User";
    private static final String LABEL_SMTP_PASSWORD = "SMTP Password";
    private static final String LABEL_SMTP_PORT = "SMTP Port";
    private static final String LABEL_SMTP_CONNECTION_TIMEOUT = "SMTP Connection Timeout";
    private static final String LABEL_SMTP_TIMEOUT = "SMTP Timeout";
    private static final String LABEL_SMTP_WRITE_TIMEOUT = "SMTP Write Timeout";
    private static final String LABEL_SMTP_LOCALHOST = "SMTP Localhost";
    private static final String LABEL_SMTP_LOCAL_ADDRESS = "SMTP Local Address";
    private static final String LABEL_SMTP_LOCAL_PORT = "SMTP Local Port";
    private static final String LABEL_SMTP_EHLO = "SMTP Ehlo";
    private static final String LABEL_SMTP_AUTH_MECHANISMS = "SMTP Auth Mechanisms";
    private static final String LABEL_SMTP_AUTH_LOGIN_DISABLE = "SMTP Auth Login Disable";
    private static final String LABEL_SMTP_AUTH_PLAIN_DISABLE = "SMTP Auth Plain Disable";
    private static final String LABEL_SMTP_AUTH_DIGEST_MD5_DISABLE = "SMTP Auth Digest MD5 Disable";
    private static final String LABEL_SMTP_AUTH_NTLM_DISABLE = "SMTP Auth Ntlm Disable";
    private static final String LABEL_SMTP_AUTH_NTLM_DOMAIN = "SMTP Auth Ntlm Domain";
    private static final String LABEL_SMTP_AUTH_NTLM_FLAGS = "SMTP Auth Ntlm Flags";
    private static final String LABEL_SMTP_AUTH_XOAUTH2_DISABLE = "SMTP Auth XOAuth2 Disable";
    private static final String LABEL_SMTP_SUBMITTER = "SMTP Submitter";
    private static final String LABEL_SMTP_DSN_NOTIFY = "SMTP DSN Notify";
    private static final String LABEL_SMTP_DSN_RET = "SMTP DSN Ret";
    private static final String LABEL_SMTP_ALLOW_8_BIT_MIME = "SMTP Allow 8-bit Mime";
    private static final String LABEL_SMTP_SEND_PARTIAL = "SMTP Send Partial";
    private static final String LABEL_SMTP_SASL_ENABLE = "SMTP SASL Enable";
    private static final String LABEL_SMTP_SASL_MECHANISMS = "SMTP SASL Mechanisms";
    private static final String LABEL_SMTP_SASL_AUTHORIZATION_ID = "SMTP SASL Authorization ID";
    private static final String LABEL_SMTP_SASL_REALM = "SMTP SASL Realm";
    private static final String LABEL_SMTP_SASL_USE_CANONICAL_HOSTNAME = "SMTP SASL Use Canonical Hostname";
    private static final String LABEL_SMTP_QUIT_WAIT = "SMTP Quit Wait";
    private static final String LABEL_SMTP_REPORT_SUCCESS = "SMTP Report Success";
    private static final String LABEL_SMTP_SSL_ENABLE = "SMTP SSL Enable";
    private static final String LABEL_SMTP_SSL_CHECK_SERVER_IDENTITY = "SMTP SSL Check Server Identity";
    private static final String LABEL_SMTP_SSL_TRUST = "SMTP SSL Trust";
    private static final String LABEL_SMTP_SSL_PROTOCOLS = "SMTP SSL Protocols";
    private static final String LABEL_SMTP_SSL_CIPHER_SUITES = "SMTP SSL Cipher Suites";
    private static final String LABEL_SMTP_START_TLS_ENABLED = "SMTP Start TLS Enabled";
    private static final String LABEL_SMTP_START_TLS_REQUIRED = "SMTP Start TLS Required";
    private static final String LABEL_SMTP_PROXY_HOST = "SMTP Proxy Host";
    private static final String LABEL_SMTP_PROXY_PORT = "SMTP Proxy Port";
    private static final String LABEL_SMTP_SOCKS_HOST = "SMTP Socks Host";
    private static final String LABEL_SMTP_SOCKS_PORT = "SMTP Socks Port";
    private static final String LABEL_SMTP_MAIL_EXTENSION = "SMTP Mail Extension";
    private static final String LABEL_SMTP_USE_RSET = "SMTP Use RSET";
    private static final String LABEL_SMTP_NOOP_STRICT = "SMTP NoOp Strict";

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
    private static final String TEST_EMAIL_DESCRIPTION = "The email address to send a message to.";

    private final EncryptionSettingsValidator encryptionValidator;

    @Autowired
    public EmailGlobalUIConfig(EncryptionSettingsValidator encryptionValidator) {
        super(EmailDescriptor.EMAIL_LABEL, EmailDescriptor.EMAIL_DESCRIPTION, EmailDescriptor.EMAIL_URL);
        this.encryptionValidator = encryptionValidator;
    }

    @Override
    public List<ConfigField> createFields() {
        // Default fields
        ConfigField mailSmtpHost = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), LABEL_SMTP_HOST, JAVAMAIL_HOST_DESCRIPTION).applyRequired(true);
        ConfigField mailSmtpFrom = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), LABEL_SMTP_FROM, JAVAMAIL_FROM_DESCRIPTION).applyRequired(true);
        ConfigField mailSmtpUser = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), LABEL_SMTP_USER, JAVAMAIL_USER_DESCRIPTION);
        ConfigField mailSmtpPassword = new PasswordConfigField(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), LABEL_SMTP_PASSWORD, JAVAMAIL_PASSWORD_DESCRIPTION, encryptionValidator);
        ConfigField mailSmtpAuth = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), LABEL_SMTP_AUTH, JAVAMAIL_AUTH_DESCRIPTION)
                                       .applyRequiredRelatedField(mailSmtpUser.getKey())
                                       .applyRequiredRelatedField(mailSmtpPassword.getKey());

        // Advanced fields
        ConfigField mailSmtpPort = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), LABEL_SMTP_PORT, JAVAMAIL_PORT_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpConnectionTimeout = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), LABEL_SMTP_CONNECTION_TIMEOUT, JAVAMAIL_CONNECTION_TIMEOUT_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpTimeout = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), LABEL_SMTP_TIMEOUT, JAVAMAIL_TIMEOUT_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpWriteTimeout = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey(), LABEL_SMTP_WRITE_TIMEOUT, JAVAMAIL_WRITETIMEOUT_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpLocalhost = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY.getPropertyKey(), LABEL_SMTP_LOCALHOST, JAVAMAIL_LOCALHOST_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpLocalAddress = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY.getPropertyKey(), LABEL_SMTP_LOCAL_ADDRESS, JAVAMAIL_LOCALHOST_ADDRESS_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpLocalPort = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey(), LABEL_SMTP_LOCAL_PORT, JAVAMAIL_LOCALHOST_PORT_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpEhlo = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), LABEL_SMTP_EHLO, JAVAMAIL_EHLO_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpAuthMechanisms = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY.getPropertyKey(), LABEL_SMTP_AUTH_MECHANISMS, JAVAMAIL_AUTH_MECHANISMS_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpAuthLoginDisable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_LOGIN_DISABLE, JAVAMAIL_AUTH_LOGIN_DISABLE_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpAuthPlainDisable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_PLAIN_DISABLE, JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_DESCRIPTION)
                                                   .applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpAuthDigestMd5Disable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_DIGEST_MD5_DISABLE, JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_DESCRIPTION)
                                                       .applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpAuthNtlmDisable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_NTLM_DISABLE, JAVAMAIL_AUTH_NTLM_DISABLE_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpAuthNtlmDomain = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY.getPropertyKey(), LABEL_SMTP_AUTH_NTLM_DOMAIN, JAVAMAIL_AUTH_NTLM_DOMAIN_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpAuthNtlmFlags = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey(), LABEL_SMTP_AUTH_NTLM_FLAGS, JAVAMAIL_AUTH_NTLM_FLAGS_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpAuthXoauth2Disable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY.getPropertyKey(), LABEL_SMTP_AUTH_XOAUTH2_DISABLE, JAVAMAIL_AUTH_XOAUTH2_DISABLE_DESCRIPTION)
                                                     .applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSubmitter = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY.getPropertyKey(), LABEL_SMTP_SUBMITTER, JAVAMAIL_SUBMITTER_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpDnsNotify = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY.getPropertyKey(), LABEL_SMTP_DSN_NOTIFY, JAVAMAIL_DSN_NOTIFY_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpDnsRet = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY.getPropertyKey(), LABEL_SMTP_DSN_RET, JAVAMAIL_DSN_RET_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpAllow8bitmime = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY.getPropertyKey(), LABEL_SMTP_ALLOW_8_BIT_MIME, JAVAMAIL_ALLOW_8_BITMIME_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSendPartial = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY.getPropertyKey(), LABEL_SMTP_SEND_PARTIAL, JAVAMAIL_SEND_PARTIAL_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSaslEnable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY.getPropertyKey(), LABEL_SMTP_SASL_ENABLE, JAVAMAIL_SASL_ENABLE_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSaslMechanisms = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY.getPropertyKey(), LABEL_SMTP_SASL_MECHANISMS, JAVAMAIL_SASL_MECHANISMS_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSaslAuthorizationId = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY.getPropertyKey(), LABEL_SMTP_SASL_AUTHORIZATION_ID, JAVAMAIL_SASL_AUTHORIZATION_ID_DESCRIPTION)
                                                      .applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSaslRealm = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey(), LABEL_SMTP_SASL_REALM, JAVAMAIL_SASL_REALM_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSaslUseCanonicalHostname = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY.getPropertyKey(), LABEL_SMTP_SASL_USE_CANONICAL_HOSTNAME,
            JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpQuitwait = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY.getPropertyKey(), LABEL_SMTP_QUIT_WAIT, JAVAMAIL_QUITWAIT_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpReportSuccess = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY.getPropertyKey(), LABEL_SMTP_REPORT_SUCCESS, JAVAMAIL_REPORT_SUCCESS_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSslEnable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY.getPropertyKey(), LABEL_SMTP_SSL_ENABLE, JAVAMAIL_SSL_ENABLE_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSslCheckServerIdentity = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY.getPropertyKey(), LABEL_SMTP_SSL_CHECK_SERVER_IDENTITY, JAVAMAIL_SSL_CHECKSERVERIDENTITY_DESCRIPTION)
                                                         .applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSslTrust = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY.getPropertyKey(), LABEL_SMTP_SSL_TRUST, JAVAMAIL_SSL_TRUST_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSslProtocols = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY.getPropertyKey(), LABEL_SMTP_SSL_PROTOCOLS, JAVAMAIL_SSL_PROTOCOLS_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSslCipherSuites = new TextInputConfigField
                                                  (EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY.getPropertyKey(), LABEL_SMTP_SSL_CIPHER_SUITES, JAVAMAIL_SSL_CIPHERSUITES_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpStartTlsEnable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY.getPropertyKey(), LABEL_SMTP_START_TLS_ENABLED, JAVAMAIL_STARTTLS_ENABLE_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpStartTlsRequired = new CheckboxConfigField
                                                   (EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY.getPropertyKey(), LABEL_SMTP_START_TLS_REQUIRED, JAVAMAIL_STARTTLS_REQUIRED_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpProxyHost = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY.getPropertyKey(), LABEL_SMTP_PROXY_HOST, JAVAMAIL_PROXY_HOST_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpProxyPort = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey(), LABEL_SMTP_PROXY_PORT, JAVAMAIL_PROXY_PORT_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSocksHost = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY.getPropertyKey(), LABEL_SMTP_SOCKS_HOST, JAVAMAIL_SOCKS_HOST_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpSocksPort = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey(), LABEL_SMTP_SOCKS_PORT, JAVAMAIL_SOCKS_PORT_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpMailExtension = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY.getPropertyKey(), LABEL_SMTP_MAIL_EXTENSION, JAVAMAIL_MAILEXTENSION_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpUserSet = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_USERSET_KEY.getPropertyKey(), LABEL_SMTP_USE_RSET, JAVAMAIL_USERSET_DESCRIPTION).applyPanel(ADVANCED_PANEL);
        ConfigField mailSmtpNoopStrict = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY.getPropertyKey(), LABEL_SMTP_NOOP_STRICT, JAVAMAIL_NOOP_STRICT_DESCRIPTION).applyPanel(ADVANCED_PANEL);

        return List.of(mailSmtpHost, mailSmtpFrom, mailSmtpAuth, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpWriteTimeout, mailSmtpLocalhost, mailSmtpLocalAddress,
            mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable, mailSmtpAuthNtlmDomain, mailSmtpAuthNtlmFlags,
            mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId, mailSmtpSaslRealm,
            mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust, mailSmtpSslProtocols, mailSmtpSslCipherSuites, mailSmtpStartTlsEnable,
            mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort, mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict
        );
    }

    @Override
    public List<ConfigField> createTestFields() {
        ConfigField emailAddress = new TextInputConfigField(TestAction.KEY_DESTINATION_NAME, TEST_LABEL_ADDRESS, TEST_EMAIL_DESCRIPTION);
        return List.of(emailAddress);
    }
}
