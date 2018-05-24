/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.AbstractChannelPropertyManager;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepositoryWrapper;

@Component
public class EmailChannelPropertyManager extends AbstractChannelPropertyManager {
    public final static String EMAIL_CHANNEL_PROPERTY_PREFIX = AbstractChannelPropertyManager.CHANNEL_PROPERTY_PREFIX + ".email.global";
    private final static Logger logger = LoggerFactory.getLogger(EmailChannelPropertyManager.class);
    private final GlobalEmailRepositoryWrapper globalEmailRepository;

    @Autowired
    public EmailChannelPropertyManager(final GlobalEmailRepositoryWrapper globalEmailRepository) {
        this.globalEmailRepository = globalEmailRepository;
    }

    @Value("blackduck.alert.channel.email.global.mail_smtp_host")
    private String mailSmtpHost;

    @Value("blackduck.alert.channel.email.global.mail_smtp_user")
    private String mailSmtpUser;

    @Value("blackduck.alert.channel.email.global.mail_smtp_password")
    private String mailSmtpPassword;

    @Value("blackduck.alert.channel.email.global.mail_smtp_port")
    private Integer mailSmtpPort;

    @Value("blackduck.alert.channel.email.global.mail_smtp_connection_timeout")
    private Integer mailSmtpConnectionTimeout;

    @Value("blackduck.alert.channel.email.global.mail_smtp_timeout")
    private Integer mailSmtpTimeout;

    @Value("blackduck.alert.channel.email.global.mail_smtp_writetimeout")
    private Integer mailSmtpWriteTimeout;

    @Value("blackduck.alert.channel.email.global.mail_smtp_from")
    private String mailSmtpFrom;

    @Value("blackduck.alert.channel.email.global.mail_smtp_localhost")
    private String mailSmtpLocalhost;

    @Value("blackduck.alert.channel.email.global.mail_smtp_localaddress")
    private String mailSmtpLocalAddress;

    @Value("blackduck.alert.channel.email.global.mail_smtp_localport")
    private Integer mailSmtpLocalPort;

    @Value("blackduck.alert.channel.email.global.mail_smtp_ehlo")
    private Boolean mailSmtpEhlo;

    @Value("blackduck.alert.channel.email.global.mail_smtp_auth")
    private Boolean mailSmtpAuth;

    @Value("blackduck.alert.channel.email.global.mail_smtp_auth_mechanisms")
    private String mailSmtpAuthMechanisms;

    @Value("blackduck.alert.channel.email.global.mail_smtp_auth_login_disable")
    private Boolean mailSmtpAuthLoginDisable;

    @Value("blackduck.alert.channel.email.global.mail_smtp_auth_plain_disable")
    private Boolean mailSmtpAuthPlainDisable;

    @Value("blackduck.alert.channel.email.global.mail_smtp_auth_digest_md5_disable")
    private Boolean mailSmtpAuthDigestMd5Disable;

    @Value("blackduck.alert.channel.email.global.mail_smtp_auth_ntlm_disable")
    private Boolean mailSmtpAuthNtlmDisable;

    @Value("blackduck.alert.channel.email.global.mail_smtp_auth_ntlm_domain")
    private String mailSmtpAuthNtlmDomain;

    @Value("blackduck.alert.channel.email.global.mail_smtp_auth_ntlm_flags")
    private Integer mailSmtpAuthNtlmFlags;

    @Value("blackduck.alert.channel.email.global.mail_smtp_auth_xoauth2_disable")
    private Boolean mailSmtpAuthXoauth2Disable;

    @Value("blackduck.alert.channel.email.global.mail_smtp_submitter")
    private String mailSmtpSubmitter;

    @Value("blackduck.alert.channel.email.global.mail_smtp_dsn_notify")
    private String mailSmtpDnsNotify;

    @Value("blackduck.alert.channel.email.global.mail_smtp_dns_ret")
    private String mailSmtpDnsRet;

    @Value("blackduck.alert.channel.email.global.mail_smtp_allow_8_bitmime")
    private Boolean mailSmtpAllow8bitmime;

    @Value("blackduck.alert.channel.email.global.mail_smtp_send_partial")
    private Boolean mailSmtpSendPartial;

    @Value("blackduck.alert.channel.email.global.mail_smtp_sasl_enable")
    private Boolean mailSmtpSaslEnable;

    @Value("blackduck.alert.channel.email.global.mail_smtp_sasl_mechanisms")
    private String mailSmtpSaslMechanisms;

    @Value("blackduck.alert.channel.email.global.mail_smtp_sasl_authorizationid")
    private String mailSmtpSaslAuthorizationId;

    @Value("blackduck.alert.channel.email.global.mail_smtp_sasl_realm")
    private String mailSmtpSaslRealm;

    @Value("blackduck.alert.channel.email.global.mail_smtp_sasl_usecanonicalhostname")
    private Boolean mailSmtpSaslUseCanonicalHostname;

    @Value("blackduck.alert.channel.email.global.mail_smtp_quitwait")
    private Boolean mailSmtpQuitwait;

    @Value("blackduck.alert.channel.email.global.mail_smtp_reportsuccess")
    private Boolean mailSmtpReportSuccess;

    @Value("blackduck.alert.channel.email.global.mail_smtp_ssl_enable")
    private Boolean mailSmtpSslEnable;

    @Value("blackduck.alert.channel.email.global.mail_smtp_ssl_checkserveridentity")
    private Boolean mailSmtpSslCheckServerIdentity;

    @Value("blackduck.alert.channel.email.global.mail_smtp_ssl_trust")
    private String mailSmtpSslTrust;

    @Value("blackduck.alert.channel.email.global.mail_smtp_ssl_protocols")
    private String mailSmtpSslProtocols;

    @Value("blackduck.alert.channel.email.global.mail_smtp_ssl_ciphersuites")
    private String mailSmtpSslCipherSuites;

    @Value("blackduck.alert.channel.email.global.mail_smtp_starttls_enable")
    private Boolean mailSmtpStartTlsEnable;

    @Value("blackduck.alert.channel.email.global.mail_smtp_starttls_required")
    private Boolean mailSmtpStartTlsRequired;

    @Value("blackduck.alert.channel.email.global.mail_smtp_proxy_host")
    private String mailSmtpProxyHost;

    @Value("blackduck.alert.channel.email.global.mail_smtp_proxy_port")
    private Integer mailSmtpProxyPort;

    @Value("blackduck.alert.channel.email.global.mail_smtp_socks_host")
    private String mailSmtpSocksHost;

    @Value("blackduck.alert.channel.email.global.mail_smtp_socks_port")
    private Integer mailSmtpSocksPort;

    @Value("blackduck.alert.channel.email.global.mail_smtp_mailextension")
    private String mailSmtpMailExtension;

    @Value("blackduck.alert.channel.email.global.mail_smtp_userset")
    private Boolean mailSmtpUserSet;

    @Value("blackduck.alert.channel.email.global.mail_smtp_noop_strict")
    private Boolean mailSmtpNoopStrict;

    @Override
    public void process() {
        logger.info("########## Email channel property manager process method called ##############");
        final GlobalEmailConfigEntity emailConfigEntity = new GlobalEmailConfigEntity();

        // globalEmailRepository.save(emailConfigEntity);
    }
}
