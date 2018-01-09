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
package com.blackducksoftware.integration.hub.alert.mock.model.global;

import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.google.gson.JsonObject;

public class MockEmailGlobalRestModel extends MockGlobalRestModelUtil<GlobalEmailConfigRestModel> {
    private final String mailSmtpHost;
    private final String mailSmtpUser;
    private final String mailSmtpPassword;
    private final boolean mailSmtpPasswordIsSet;
    private final String mailSmtpPort;
    private final String mailSmtpConnectionTimeout;
    private final String mailSmtpTimeout;
    private final String mailSmtpFrom;
    private final String mailSmtpLocalhost;
    private final String mailSmtpEhlo;
    private final String mailSmtpAuth;
    private final String mailSmtpDnsNotify;
    private final String mailSmtpDnsRet;
    private final String mailSmtpAllow8bitmime;
    private final String mailSmtpSendPartial;
    private final String emailTemplateDirectory;
    private final String emailTemplateLogoImage;
    private final String emailSubjectLine;
    private final String id;

    public MockEmailGlobalRestModel() {
        this("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", false, "99", "400", "500", "MailSmtpFrom", "MailSmtpLocalhost", "true", "false", "MailSmtpDnsNotify", "MailSmtpDnsRet", "true", "false", "EmailTemplateDirectory",
                "EmailTemplateLogoImage", "EmailSubjectLine", "1");
    }

    private MockEmailGlobalRestModel(final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final boolean mailSmtpPasswordIsSet, final String mailSmtpPort, final String mailSmtpConnectionTimeout,
            final String mailSmtpTimeout, final String mailSmtpFrom, final String mailSmtpLocalhost, final String mailSmtpEhlo, final String mailSmtpAuth, final String mailSmtpDnsNotify, final String mailSmtpDnsRet,
            final String mailSmtpAllow8bitmime, final String mailSmtpSendPartial, final String emailTemplateDirectory, final String emailTemplateLogoImage, final String emailSubjectLine, final String id) {
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

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public GlobalEmailConfigRestModel createGlobalRestModel() {
        final GlobalEmailConfigRestModel restModel = new GlobalEmailConfigRestModel(id, mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPasswordIsSet, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom,
                mailSmtpLocalhost, mailSmtpEhlo, mailSmtpAuth, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
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
        json.addProperty("mailSmtpPasswordIsSet", mailSmtpPasswordIsSet);
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

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("mailSmtpPasswordIsSet", false);
        return json.toString();
    }

}
