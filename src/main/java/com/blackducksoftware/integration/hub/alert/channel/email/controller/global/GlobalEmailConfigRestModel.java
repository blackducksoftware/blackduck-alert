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
package com.blackducksoftware.integration.hub.alert.channel.email.controller.global;

import com.blackducksoftware.integration.hub.alert.annotation.SensitiveField;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public class GlobalEmailConfigRestModel extends ConfigRestModel {
    private static final long serialVersionUID = 9172607945030111585L;

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
    private String mailSmtpFrom;
    private String mailSmtpLocalhost;
    private String mailSmtpEhlo;
    private String mailSmtpAuth;
    private String mailSmtpDnsNotify;
    private String mailSmtpDnsRet;
    private String mailSmtpAllow8bitmime;
    private String mailSmtpSendPartial;
    private String emailTemplateDirectory;
    private String emailTemplateLogoImage;
    private String emailSubjectLine;

    public GlobalEmailConfigRestModel() {
    }

    public GlobalEmailConfigRestModel(final String id, final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final boolean mailSmtpPasswordIsSet, final String mailSmtpPort,
            final String mailSmtpConnectionTimeout, final String mailSmtpTimeout, final String mailSmtpFrom, final String mailSmtpLocalhost, final String mailSmtpEhlo, final String mailSmtpAuth, final String mailSmtpDnsNotify,
            final String mailSmtpDnsRet, final String mailSmtpAllow8bitmime, final String mailSmtpSendPartial, final String emailTemplateDirectory, final String emailTemplateLogoImage, final String emailSubjectLine) {
        super(id);
        this.mailSmtpHost = mailSmtpHost;
        this.mailSmtpUser = mailSmtpUser;
        this.mailSmtpPassword = mailSmtpPassword;
        this.mailSmtpPasswordIsSet = mailSmtpPasswordIsSet;
        this.mailSmtpPort = mailSmtpPort;
        this.mailSmtpConnectionTimeout = mailSmtpConnectionTimeout;
        this.mailSmtpTimeout = mailSmtpTimeout;
        this.mailSmtpFrom = mailSmtpFrom;
        this.mailSmtpLocalhost = mailSmtpLocalhost;
        this.mailSmtpEhlo = mailSmtpEhlo;
        this.mailSmtpAuth = mailSmtpAuth;
        this.mailSmtpDnsNotify = mailSmtpDnsNotify;
        this.mailSmtpDnsRet = mailSmtpDnsRet;
        this.mailSmtpAllow8bitmime = mailSmtpAllow8bitmime;
        this.mailSmtpSendPartial = mailSmtpSendPartial;
        this.emailTemplateDirectory = emailTemplateDirectory;
        this.emailTemplateLogoImage = emailTemplateLogoImage;
        this.emailSubjectLine = emailSubjectLine;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
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

    public Boolean isMailSmtpPasswordIsSet() {
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

    public String getMailSmtpFrom() {
        return mailSmtpFrom;
    }

    public String getMailSmtpLocalhost() {
        return mailSmtpLocalhost;
    }

    public String getMailSmtpEhlo() {
        return mailSmtpEhlo;
    }

    public String getMailSmtpAuth() {
        return mailSmtpAuth;
    }

    public String getMailSmtpDnsNotify() {
        return mailSmtpDnsNotify;
    }

    public String getMailSmtpDnsRet() {
        return mailSmtpDnsRet;
    }

    public String getMailSmtpAllow8bitmime() {
        return mailSmtpAllow8bitmime;
    }

    public String getMailSmtpSendPartial() {
        return mailSmtpSendPartial;
    }

    public String getEmailTemplateDirectory() {
        return emailTemplateDirectory;
    }

    public String getEmailTemplateLogoImage() {
        return emailTemplateLogoImage;
    }

    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

}
