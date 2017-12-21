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
package com.blackducksoftware.integration.hub.alert.mock;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalEmailConfigRestModel;
import com.google.gson.JsonObject;

public class EmailMockUtils extends DistributionMockUtils implements MockUtils<EmailGroupDistributionRestModel, GlobalEmailConfigRestModel, EmailGroupDistributionConfigEntity, GlobalEmailConfigEntity> {
    private final String mailSmtpHost;
    private final String mailSmtpUser;
    private final String mailSmtpPassword;
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
    private final String groupName;
    private final String id;

    public EmailMockUtils() {
        this("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", "99", "400", "500", "MailSmtpFrom", "MailSmtpLocalhost", "true", "false", "MailSmtpDnsNotify", "MailSmtpDnsRet", "true", "false", "EmailTemplateDirectory",
                "EmailTemplateLogoImage", "EmailSubjectLine", "EmailGroup", "1");
    }

    public EmailMockUtils(final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final String mailSmtpPort, final String mailSmtpConnectionTimeout, final String mailSmtpTimeout, final String mailSmtpFrom,
            final String mailSmtpLocalhost, final String mailSmtpEhlo, final String mailSmtpAuth, final String mailSmtpDnsNotify, final String mailSmtpDnsRet, final String mailSmtpAllow8bitmime, final String mailSmtpSendPartial,
            final String emailTemplateDirectory, final String emailTemplateLogoImage, final String emailSubjectLine, final String emailGroup, final String id) {
        super(id);
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
        this.mailSmtpDnsRet = mailSmtpDnsRet;
        this.mailSmtpAllow8bitmime = mailSmtpAllow8bitmime;
        this.mailSmtpSendPartial = mailSmtpSendPartial;
        this.emailTemplateDirectory = emailTemplateDirectory;
        this.emailTemplateLogoImage = emailTemplateLogoImage;
        this.emailSubjectLine = emailSubjectLine;
        this.groupName = emailGroup;
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

    public String getEmailGroup() {
        return groupName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public GlobalEmailConfigRestModel createGlobalRestModel() {
        final GlobalEmailConfigRestModel restModel = new GlobalEmailConfigRestModel(id, mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo,
                mailSmtpAuth, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        return restModel;
    }

    @Override
    public GlobalEmailConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalEmailConfigRestModel();
    }

    @Override
    public EmailGroupDistributionRestModel createRestModel() {
        final EmailGroupDistributionRestModel restModel = new EmailGroupDistributionRestModel(getCommonId(), getDistributionConfigId(), getDistributionType(), getName(), getFrequency(), getFilterByProject(), groupName, getProjects(),
                getNotifications());
        return restModel;
    }

    @Override
    public EmailGroupDistributionRestModel createEmptyRestModel() {
        return new EmailGroupDistributionRestModel();
    }

    @Override
    public GlobalEmailConfigEntity createGlobalEntity() {
        final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, Integer.valueOf(mailSmtpPort), Integer.valueOf(mailSmtpConnectionTimeout), Integer.valueOf(mailSmtpTimeout),
                mailSmtpFrom, mailSmtpLocalhost, Boolean.valueOf(mailSmtpEhlo), Boolean.valueOf(mailSmtpAuth), mailSmtpDnsNotify, mailSmtpDnsRet, Boolean.valueOf(mailSmtpAllow8bitmime), Boolean.valueOf(mailSmtpSendPartial),
                emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        entity.setId(Long.parseLong(id));
        return entity;
    }

    @Override
    public GlobalEmailConfigEntity createEmptyGlobalEntity() {
        return new GlobalEmailConfigEntity();
    }

    @Override
    public EmailGroupDistributionConfigEntity createEntity() {
        final EmailGroupDistributionConfigEntity entity = new EmailGroupDistributionConfigEntity(groupName);
        entity.setId(Long.parseLong(id));
        return entity;
    }

    @Override
    public EmailGroupDistributionConfigEntity createEmptyEntity() {
        return new EmailGroupDistributionConfigEntity();
    }

    @Override
    public String getGlobalRestModelJson() {
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

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.add("mailSmtpHost", null);
        json.add("mailSmtpUser", null);
        json.add("mailSmtpPort", null);
        json.add("mailSmtpConnectionTimeout", null);
        json.add("mailSmtpTimeout", null);
        json.add("mailSmtpFrom", null);
        json.add("mailSmtpLocalhost", null);
        json.add("mailSmtpEhlo", null);
        json.add("mailSmtpAuth", null);
        json.add("mailSmtpDnsNotify", null);
        json.add("mailSmtpUser", null);
        json.add("mailSmtpPort", null);
        json.add("mailSmtpConnectionTimeout", null);
        json.add("mailSmtpDnsRet", null);
        json.add("mailSmtpAllow8bitmime", null);
        json.add("mailSmtpSendPartial", null);
        json.add("emailTemplateDirectory", null);
        json.add("emailTemplateLogoImage", null);
        json.add("mailSmtpAllow8bitmime", null);
        json.add("emailSubjectLine", null);
        json.add("id", null);
        return json.toString();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("groupName", groupName);
        getDistributionRestModelJson(json);
        return json.toString();
    }

    @Override
    public String getEmptyRestModelJson() {
        final JsonObject json = new JsonObject();
        json.add("groupName", null);
        getEmptyDistributionRestModelJson(json);
        return json.toString();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("mailSmtpHost", mailSmtpHost);
        json.addProperty("mailSmtpUser", mailSmtpUser);
        json.addProperty("mailSmtpPort", Integer.valueOf(mailSmtpPort));
        json.addProperty("mailSmtpConnectionTimeout", Integer.valueOf(mailSmtpConnectionTimeout));
        json.addProperty("mailSmtpTimeout", Integer.valueOf(mailSmtpTimeout));
        json.addProperty("mailSmtpFrom", mailSmtpFrom);
        json.addProperty("mailSmtpLocalhost", mailSmtpLocalhost);
        json.addProperty("mailSmtpEhlo", Boolean.valueOf(mailSmtpEhlo));
        json.addProperty("mailSmtpAuth", Boolean.valueOf(mailSmtpAuth));
        json.addProperty("mailSmtpDnsNotify", mailSmtpDnsNotify);
        json.addProperty("mailSmtpDnsRet", mailSmtpDnsRet);
        json.addProperty("mailSmtpAllow8bitmime", Boolean.valueOf(mailSmtpAllow8bitmime));
        json.addProperty("mailSmtpSendPartial", Boolean.valueOf(mailSmtpSendPartial));
        json.addProperty("emailTemplateDirectory", emailTemplateDirectory);
        json.addProperty("emailTemplateLogoImage", emailTemplateLogoImage);
        json.addProperty("emailSubjectLine", emailSubjectLine);
        json.addProperty("id", Long.valueOf(id));
        return json.toString();
    }

    @Override
    public String getEmptyGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.add("mailSmtpHost", null);
        json.add("mailSmtpUser", null);
        json.add("mailSmtpPort", null);
        json.add("mailSmtpConnectionTimeout", null);
        json.add("mailSmtpTimeout", null);
        json.add("mailSmtpFrom", null);
        json.add("mailSmtpLocalhost", null);
        json.add("mailSmtpEhlo", null);
        json.add("mailSmtpAuth", null);
        json.add("mailSmtpDnsNotify", null);
        json.add("mailSmtpUser", null);
        json.add("mailSmtpPort", null);
        json.add("mailSmtpConnectionTimeout", null);
        json.add("mailSmtpDnsRet", null);
        json.add("mailSmtpAllow8bitmime", null);
        json.add("mailSmtpSendPartial", null);
        json.add("emailTemplateDirectory", null);
        json.add("emailTemplateLogoImage", null);
        json.add("mailSmtpAllow8bitmime", null);
        json.add("emailSubjectLine", null);
        json.add("id", null);
        return json.toString();
    }

    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("groupName", groupName);
        json.addProperty("id", Long.valueOf(id));
        return json.toString();
    }

    @Override
    public String getEmptyEntityJson() {
        final JsonObject json = new JsonObject();
        json.add("groupName", null);
        json.add("id", null);
        return json.toString();
    }

}
