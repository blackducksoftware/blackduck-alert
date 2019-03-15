/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
import com.synopsys.integration.alert.common.enumeration.FieldGroup;

@Component
public class EmailGlobalUIConfig extends UIConfig {
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

    public EmailGlobalUIConfig() {
        super(EmailDescriptor.EMAIL_LABEL, EmailDescriptor.EMAIL_DESCRIPTION, EmailDescriptor.EMAIL_URL, EmailDescriptor.EMAIL_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        // Default fields
        final ConfigField mailSmtpHost = TextInputConfigField.createRequired(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), "Smtp Host", JAVAMAIL_HOST_DESCRIPTION);
        final ConfigField mailSmtpFrom = TextInputConfigField.createRequired(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), "Smtp From", JAVAMAIL_FROM_DESCRIPTION);
        final ConfigField mailSmtpAuth = CheckboxConfigField.create(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), "Smtp Auth", JAVAMAIL_AUTH_DESCRIPTION);
        final ConfigField mailSmtpUser = TextInputConfigField.create(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), "Smtp User", JAVAMAIL_USER_DESCRIPTION);
        final ConfigField mailSmtpPassword = PasswordConfigField.create(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), "Smtp Password", JAVAMAIL_PASSWORD_DESCRIPTION);

        // Advanced fields
        final ConfigField mailSmtpPort = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "Smtp Port", JAVAMAIL_PORT_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpConnectionTimeout = NumberConfigField
                                                          .createGrouped(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "Smtp Connection Timeout", JAVAMAIL_CONNECTION_TIMEOUT_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpTimeout = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "Smtp Timeout", JAVAMAIL_TIMEOUT_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpWriteTimeout = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey(), "Smtp Write Timeout", JAVAMAIL_WRITETIMEOUT_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalhost = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY.getPropertyKey(), "Smtp Localhost", JAVAMAIL_LOCALHOST_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalAddress = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY.getPropertyKey(), "Smtp Local Address", JAVAMAIL_LOCALHOST_ADDRESS_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalPort = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey(), "Smtp Local Port", JAVAMAIL_LOCALHOST_PORT_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpEhlo = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), "Smtp Ehlo", JAVAMAIL_EHLO_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthMechanisms = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY.getPropertyKey(), "Smtp Auth Mechanisms", JAVAMAIL_AUTH_MECHANISMS_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthLoginDisable = CheckboxConfigField
                                                         .createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY.getPropertyKey(), "Smtp Auth Login Disable", JAVAMAIL_AUTH_LOGIN_DISABLE_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthPlainDisable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY.getPropertyKey(), "Smtp Auth Plain Disable", JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_DESCRIPTION,
            FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthDigestMd5Disable = CheckboxConfigField
                                                             .createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY.getPropertyKey(), "Smtp Auth Digest MD5 Disable", JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_DESCRIPTION,
                                                                 FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDisable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY.getPropertyKey(), "Smtp Auth Ntlm Disable", JAVAMAIL_AUTH_NTLM_DISABLE_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDomain = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY.getPropertyKey(), "Smtp Auth Ntlm Domain", JAVAMAIL_AUTH_NTLM_DOMAIN_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmFlags = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey(), "Smtp Auth Ntlm Flags", JAVAMAIL_AUTH_NTLM_FLAGS_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthXoauth2Disable = CheckboxConfigField
                                                           .createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY.getPropertyKey(), "SMTP Auth XOAuth2 Disable", JAVAMAIL_AUTH_XOAUTH2_DISABLE_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSubmitter = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY.getPropertyKey(), "Smtp Submitter", JAVAMAIL_SUBMITTER_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsNotify = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY.getPropertyKey(), "Smtp DSN Notify", JAVAMAIL_DSN_NOTIFY_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsRet = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY.getPropertyKey(), "Smtp DSN Ret", JAVAMAIL_DSN_RET_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAllow8bitmime = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY.getPropertyKey(), "Smtp Allow 8-bit Mime", JAVAMAIL_ALLOW_8_BITMIME_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSendPartial = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY.getPropertyKey(), "Smtp Send Partial", JAVAMAIL_SEND_PARTIAL_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslEnable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY.getPropertyKey(), "Smtp SASL Enable", JAVAMAIL_SASL_ENABLE_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslMechanisms = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY.getPropertyKey(), "Smtp SASL Mechanisms", JAVAMAIL_SASL_MECHANISMS_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslAuthorizationId = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY.getPropertyKey(), "Smtp SASL Authorization ID", JAVAMAIL_SASL_AUTHORIZATION_ID_DESCRIPTION,
            FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslRealm = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey(), "Smtp SASL Realm", JAVAMAIL_SASL_REALM_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslUseCanonicalHostname = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY.getPropertyKey(), "Smtp SASL Use Canonical Hostname",
            JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpQuitwait = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY.getPropertyKey(), "Smtp Quit Wait", JAVAMAIL_QUITWAIT_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpReportSuccess = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY.getPropertyKey(), "Smtp Report Success", JAVAMAIL_REPORT_SUCCESS_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslEnable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY.getPropertyKey(), "Smtp SSL Enable", JAVAMAIL_SSL_ENABLE_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCheckServerIdentity = CheckboxConfigField
                                                               .createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY.getPropertyKey(), "Smtp SSL Check Server Identity", JAVAMAIL_SSL_CHECKSERVERIDENTITY_DESCRIPTION,
                                                                   FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslTrust = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY.getPropertyKey(), "Smtp SSL Trust", JAVAMAIL_SSL_TRUST_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslProtocols = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY.getPropertyKey(), "Smtp SSL Protocols", JAVAMAIL_SSL_PROTOCOLS_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCipherSuites = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY.getPropertyKey(), "Smtp SSL Cipher Suites", JAVAMAIL_SSL_CIPHERSUITES_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsEnable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY.getPropertyKey(), "Smtp Start TLS Enabled", JAVAMAIL_STARTTLS_ENABLE_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsRequired = CheckboxConfigField
                                                         .createGrouped(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY.getPropertyKey(), "Smtp Start TLS Required", JAVAMAIL_STARTTLS_REQUIRED_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyHost = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY.getPropertyKey(), "Smtp Proxy Host", JAVAMAIL_PROXY_HOST_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyPort = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey(), "Smtp Proxy Port", JAVAMAIL_PROXY_PORT_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksHost = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY.getPropertyKey(), "Smtp Socks Host", JAVAMAIL_SOCKS_HOST_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksPort = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey(), "Smtp Socks Port", JAVAMAIL_SOCKS_PORT_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpMailExtension = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY.getPropertyKey(), "Smtp Mail Extension", JAVAMAIL_MAILEXTENSION_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpUserSet = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_USERSET_KEY.getPropertyKey(), "Smtp Use RSET", JAVAMAIL_USERSET_DESCRIPTION, FieldGroup.ADVANCED);
        final ConfigField mailSmtpNoopStrict = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY.getPropertyKey(), "Smtp NoOp Strict", JAVAMAIL_NOOP_STRICT_DESCRIPTION, FieldGroup.ADVANCED);

        return List.of(mailSmtpHost, mailSmtpFrom, mailSmtpAuth, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpWriteTimeout, mailSmtpLocalhost, mailSmtpLocalAddress,
            mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable, mailSmtpAuthNtlmDomain, mailSmtpAuthNtlmFlags,
            mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId, mailSmtpSaslRealm,
            mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust, mailSmtpSslProtocols, mailSmtpSslCipherSuites, mailSmtpStartTlsEnable,
            mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort, mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict
        );
    }
}
