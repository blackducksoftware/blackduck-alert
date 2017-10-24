/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.datasource.repository;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Entity
@Table(name = "email_config", schema = "configuration")
public class EmailConfigEntity implements Serializable {
    private static final long serialVersionUID = 9172607945030111585L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private final Long id;

    // JavaMail properties http://connector.sourceforge.net/doc-files/Properties.html
    @Column(name = "mail_smtp_host")
    private final String mailSmtpHost;

    @Column(name = "mail_smtp_user")
    private final String mailSmtpUser;

    // not a javamail property, but we are going to piggy-back to get the smtp password
    @Column(name = "mail_smtp_password")
    private final String mailSmtpPassword;

    @Column(name = "mail_smtp_port")
    private final Integer mailSmtpPort;

    @Column(name = "mail_smtp_connection_timeout")
    private final Integer mailSmtpConnectionTimeout;

    @Column(name = "mail_smtp_timeout")
    private final Integer mailSmtpTimeout;

    @Column(name = "mail_smtp_from")
    private final String mailSmtpFrom;

    @Column(name = "mail_smtp_localhost")
    private final String mailSmtpLocalhost;

    @Column(name = "mail_smtp_ehlo")
    private final Boolean mailSmtpEhlo;

    @Column(name = "mail_smtp_auth")
    private final Boolean mailSmtpAuth;

    @Column(name = "mail_smtp_dsn_notify")
    private final String mailSmtpDnsNotify;

    @Column(name = "mail_smtp_dsn_ret")
    private final String mailSmtpDsnRet;

    @Column(name = "mail_smtp_allow_8_bitmime")
    private final Boolean mailSmtpAllow8bitmime;

    @Column(name = "mail_smtp_send_partial")
    private final Boolean mailSmtpSendPartial;

    @Column(name = "email_template_directory")
    private final String emailTemplateDirectory;

    @Column(name = "email_template_logo_image")
    private final String emailTemplateLogoImage;

    public EmailConfigEntity(final Long id, final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final Integer mailSmtpPort, final Integer mailSmtpConnectionTimeout, final Integer mailSmtpTimeout,
            final String mailSmtpFrom, final String mailSmtpLocalhost, final Boolean mailSmtpEhlo, final Boolean mailSmtpAuth, final String mailSmtpDnsNotify, final String mailSmtpDsnRet, final Boolean mailSmtpAllow8bitmime,
            final Boolean mailSmtpSendPartial, final String emailTemplateDirectory, final String emailTemplateLogoImage) {
        this.id = id;
        this.mailSmtpHost = mailSmtpHost;
        this.mailSmtpUser = mailSmtpUser;
        this.mailSmtpPassword = mailSmtpPassword;
        this.mailSmtpPort = mailSmtpPort;
        this.mailSmtpConnectionTimeout = mailSmtpConnectionTimeout;
        this.mailSmtpTimeout = mailSmtpTimeout;
        this.mailSmtpFrom = mailSmtpFrom;
        this.mailSmtpLocalhost = mailSmtpLocalhost;
        this.mailSmtpEhlo = mailSmtpEhlo;
        this.mailSmtpAuth = mailSmtpAuth;
        this.mailSmtpDnsNotify = mailSmtpDnsNotify;
        this.mailSmtpDsnRet = mailSmtpDsnRet;
        this.mailSmtpAllow8bitmime = mailSmtpAllow8bitmime;
        this.mailSmtpSendPartial = mailSmtpSendPartial;
        this.emailTemplateDirectory = emailTemplateDirectory;
        this.emailTemplateLogoImage = emailTemplateLogoImage;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
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

    public String getMailSmtpFrom() {
        return mailSmtpFrom;
    }

    public String getMailSmtpLocalhost() {
        return mailSmtpLocalhost;
    }

    public Boolean getMailSmtpEhlo() {
        return mailSmtpEhlo;
    }

    public Boolean getMailSmtpAuth() {
        return mailSmtpAuth;
    }

    public String getMailSmtpDnsNotify() {
        return mailSmtpDnsNotify;
    }

    public String getMailSmtpDsnRet() {
        return mailSmtpDsnRet;
    }

    public Boolean getMailSmtpAllow8bitmime() {
        return mailSmtpAllow8bitmime;
    }

    public Boolean getMailSmtpSendPartial() {
        return mailSmtpSendPartial;
    }

    public String getEmailTemplateDirectory() {
        return emailTemplateDirectory;
    }

    public String getEmailTemplateLogoImage() {
        return emailTemplateLogoImage;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        reflectionToStringBuilder.setExcludeFieldNames("mailSmtpPassword");
        return reflectionToStringBuilder.toString();
    }
}
