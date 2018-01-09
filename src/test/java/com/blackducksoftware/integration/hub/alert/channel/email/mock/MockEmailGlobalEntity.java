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
package com.blackducksoftware.integration.hub.alert.channel.email.mock;

import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;
import com.google.gson.JsonObject;

public class MockEmailGlobalEntity extends MockGlobalEntityUtil<GlobalEmailConfigEntity> {
    private final String mailSmtpHost;
    private final String mailSmtpUser;
    private final String mailSmtpPassword;
    private final boolean mailSmtpPasswordIsSet;
    private final Integer mailSmtpPort;
    private final Integer mailSmtpConnectionTimeout;
    private final Integer mailSmtpTimeout;
    private final String mailSmtpFrom;
    private final String mailSmtpLocalhost;
    private final Boolean mailSmtpEhlo;
    private final Boolean mailSmtpAuth;
    private final String mailSmtpDnsNotify;
    private final String mailSmtpDnsRet;
    private final Boolean mailSmtpAllow8bitmime;
    private final Boolean mailSmtpSendPartial;
    private final String emailTemplateDirectory;
    private final String emailTemplateLogoImage;
    private final String emailSubjectLine;
    private final Long id;

    public MockEmailGlobalEntity() {
        this("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", false, 99, 400, 500, "MailSmtpFrom", "MailSmtpLocalhost", true, false, "MailSmtpDnsNotify", "MailSmtpDnsRet", true, false, "EmailTemplateDirectory", "EmailTemplateLogoImage",
                "EmailSubjectLine", 1L);
    }

    private MockEmailGlobalEntity(final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final boolean mailSmtpPasswordIsSet, final Integer mailSmtpPort, final Integer mailSmtpConnectionTimeout,
            final Integer mailSmtpTimeout, final String mailSmtpFrom, final String mailSmtpLocalhost, final Boolean mailSmtpEhlo, final Boolean mailSmtpAuth, final String mailSmtpDnsNotify, final String mailSmtpDnsRet,
            final Boolean mailSmtpAllow8bitmime, final Boolean mailSmtpSendPartial, final String emailTemplateDirectory, final String emailTemplateLogoImage, final String emailSubjectLine, final Long id) {
        super();
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
        this.id = id;
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

    public String getMailSmtpDnsRet() {
        return mailSmtpDnsRet;
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

    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public GlobalEmailConfigEntity createGlobalEntity() {
        final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, Integer.valueOf(mailSmtpPort), Integer.valueOf(mailSmtpConnectionTimeout), Integer.valueOf(mailSmtpTimeout),
                mailSmtpFrom, mailSmtpLocalhost, Boolean.valueOf(mailSmtpEhlo), Boolean.valueOf(mailSmtpAuth), mailSmtpDnsNotify, mailSmtpDnsRet, Boolean.valueOf(mailSmtpAllow8bitmime), Boolean.valueOf(mailSmtpSendPartial),
                emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        entity.setId(id);
        return entity;
    }

    @Override
    public GlobalEmailConfigEntity createEmptyGlobalEntity() {
        return new GlobalEmailConfigEntity();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("mailSmtpHost", mailSmtpHost);
        json.addProperty("mailSmtpUser", mailSmtpUser);
        json.addProperty("mailSmtpPort", mailSmtpPort);
        json.addProperty("mailSmtpConnectionTimeout", mailSmtpConnectionTimeout);
        json.addProperty("mailSmtpTimeout", mailSmtpTimeout);
        json.addProperty("mailSmtpFrom", mailSmtpFrom);
        json.addProperty("mailSmtpLocalhost", mailSmtpLocalhost);
        json.addProperty("mailSmtpEhlo", mailSmtpEhlo);
        json.addProperty("mailSmtpAuth", mailSmtpAuth);
        json.addProperty("mailSmtpDnsNotify", mailSmtpDnsNotify);
        json.addProperty("mailSmtpDnsRet", mailSmtpDnsRet);
        json.addProperty("mailSmtpAllow8bitmime", mailSmtpAllow8bitmime);
        json.addProperty("mailSmtpSendPartial", mailSmtpSendPartial);
        json.addProperty("emailTemplateDirectory", emailTemplateDirectory);
        json.addProperty("emailTemplateLogoImage", emailTemplateLogoImage);
        json.addProperty("emailSubjectLine", emailSubjectLine);
        json.addProperty("id", id);
        return json.toString();
    }

}
