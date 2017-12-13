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
    private final String emailGroup;
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
        this.emailGroup = emailGroup;
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
        return emailGroup;
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
        final EmailGroupDistributionRestModel restModel = new EmailGroupDistributionRestModel(getCommonId(), getDistributionConfigId(), getDistributionType(), getName(), getFrequency(), getNotificationType(), getFilterByProject(),
                emailGroup);
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
        final EmailGroupDistributionConfigEntity entity = new EmailGroupDistributionConfigEntity(emailGroup);
        entity.setId(Long.parseLong(id));
        return entity;
    }

    @Override
    public EmailGroupDistributionConfigEntity createEmptyEntity() {
        return new EmailGroupDistributionConfigEntity();
    }

    // 8
    // {"mailSmtpHost":"MailSmtpHost","mailSmtpUser":"MailSmtpUser","mailSmtpPort":"99","mailSmtpConnectionTimeout":"400","mailSmtpTimeout":"500","mailSmtpFrom":"MailSmtpFrom","mailSmtpLocalhost":"MailSmtpLocalhost","mailSmtpEhlo":"true","mailSmtpAuth":"false","mailSmtpDnsNotify":"MailSmtpDnsNotify","mailSmtpDnsRet":"MailSmtpDnsRet","mailSmtpAllow8bitmime":"true","mailSmtpSendPartial":"false","emailTemplateDirectory":"EmailTemplateDirectory","emailTemplateLogoImage":"EmailTemplateLogoImage","emailSubjectLine":"EmailSubjectLine","id":"1"}
    @Override
    public String getGlobalRestModelJson() {
        final StringBuilder json = new StringBuilder();
        json.append("{\"mailSmtpHost\":\"");
        json.append(mailSmtpHost);
        json.append("\",\"mailSmtpUser\":\"");
        json.append(mailSmtpUser);
        json.append("\",\"mailSmtpPort\":\"");
        json.append(mailSmtpPort);
        json.append("\",\"mailSmtpConnectionTimeout\":\"");
        json.append(mailSmtpConnectionTimeout);
        json.append("\",\"mailSmtpTimeout\":\"");
        json.append(mailSmtpTimeout);
        json.append("\",\"mailSmtpFrom\":\"");
        json.append(mailSmtpFrom);
        json.append("\",\"mailSmtpLocalhost\":\"");
        json.append(mailSmtpLocalhost);
        json.append("\",\"mailSmtpEhlo\":\"");
        json.append(mailSmtpEhlo);
        json.append("\",\"mailSmtpAuth\":\"");
        json.append(mailSmtpAuth);
        json.append("\",\"mailSmtpDnsNotify\":\"");
        json.append(mailSmtpDnsNotify);
        json.append("\",\"mailSmtpDnsRet\":\"");
        json.append(mailSmtpDnsRet);
        json.append("\",\"mailSmtpAllow8bitmime\":\"");
        json.append(mailSmtpAllow8bitmime);
        json.append("\",\"mailSmtpSendPartial\":\"");
        json.append(mailSmtpSendPartial);
        json.append("\",\"emailTemplateDirectory\":\"");
        json.append(emailTemplateDirectory);
        json.append("\",\"emailTemplateLogoImage\":\"");
        json.append(emailTemplateLogoImage);
        json.append("\",\"emailSubjectLine\":\"");
        json.append(emailSubjectLine);
        json.append("\",\"id\":\"");
        json.append(id);
        json.append("\"}");
        return json.toString();
    }

    @Override
    public String getEmptyGlobalRestModelJson() {
        return "{\"mailSmtpHost\":null,\"mailSmtpUser\":null,\"mailSmtpPort\":null,\"mailSmtpConnectionTimeout\":null,\"mailSmtpTimeout\":null,\"mailSmtpFrom\":null,\"mailSmtpLocalhost\":null,\"mailSmtpEhlo\":null,\"mailSmtpAuth\":null,\"mailSmtpDnsNotify\":null,\"mailSmtpDnsRet\":null,\"mailSmtpAllow8bitmime\":null,\"mailSmtpSendPartial\":null,\"emailTemplateDirectory\":null,\"emailTemplateLogoImage\":null,\"emailSubjectLine\":null,\"id\":null}";
    }

    @Override
    public String getRestModelJson() {
        final StringBuilder json = new StringBuilder();
        json.append("{\"groupName\":\"");
        json.append(emailGroup);
        json.append("\",");
        json.append(getDistributionRestModelJson());
        json.append("\"}");
        return json.toString();
    }

    @Override
    public String getEmptyRestModelJson() {
        return "{\"groupName\":null,\"distributionConfigId\":null,\"distributionType\":null,\"name\":null,\"frequency\":null,\"notificationType\":null,\"filterByProject\":null,\"id\":null}";
    }

    // {"mailSmtpHost":"MailSmtpHost","mailSmtpUser":"MailSmtpUser","mailSmtpPort":99,"mailSmtpConnectionTimeout":400,"mailSmtpTimeout":500,"mailSmtpFrom":"MailSmtpFrom","mailSmtpLocalhost":"MailSmtpLocalhost","mailSmtpEhlo":true,"mailSmtpAuth":false,"mailSmtpDnsNotify":"MailSmtpDnsNotify","mailSmtpDnsRet":"MailSmtpDnsRet","mailSmtpAllow8bitmime":true,"mailSmtpSendPartial":false,"emailTemplateDirectory":"EmailTemplateDirectory","emailTemplateLogoImage":"EmailTemplateLogoImage","emailSubjectLine":"EmailSubjectLine","id":1}
    @Override
    public String getGlobalEntityJson() {
        final StringBuilder json = new StringBuilder();
        json.append("{\"mailSmtpHost\":\"");
        json.append(mailSmtpHost);
        json.append("\",\"mailSmtpUser\":\"");
        json.append(mailSmtpUser);
        json.append("\",\"mailSmtpPort\":");
        json.append(mailSmtpPort);
        json.append(",\"mailSmtpConnectionTimeout\":");
        json.append(mailSmtpConnectionTimeout);
        json.append(",\"mailSmtpTimeout\":");
        json.append(mailSmtpTimeout);
        json.append(",\"mailSmtpFrom\":\"");
        json.append(mailSmtpFrom);
        json.append("\",\"mailSmtpLocalhost\":\"");
        json.append(mailSmtpLocalhost);
        json.append("\",\"mailSmtpEhlo\":");
        json.append(mailSmtpEhlo);
        json.append(",\"mailSmtpAuth\":");
        json.append(mailSmtpAuth);
        json.append(",\"mailSmtpDnsNotify\":\"");
        json.append(mailSmtpDnsNotify);
        json.append("\",\"mailSmtpDnsRet\":\"");
        json.append(mailSmtpDnsRet);
        json.append("\",\"mailSmtpAllow8bitmime\":");
        json.append(mailSmtpAllow8bitmime);
        json.append(",\"mailSmtpSendPartial\":");
        json.append(mailSmtpSendPartial);
        json.append(",\"emailTemplateDirectory\":\"");
        json.append(emailTemplateDirectory);
        json.append("\",\"emailTemplateLogoImage\":\"");
        json.append(emailTemplateLogoImage);
        json.append("\",\"emailSubjectLine\":\"");
        json.append(emailSubjectLine);
        json.append("\",\"id\":");
        json.append(id);
        json.append("}");
        return json.toString();
    }

    @Override
    public String getEmptyGlobalEntityJson() {
        return "{\"mailSmtpHost\":null,\"mailSmtpUser\":null,\"mailSmtpPort\":null,\"mailSmtpConnectionTimeout\":null,\"mailSmtpTimeout\":null,\"mailSmtpFrom\":null,\"mailSmtpLocalhost\":null,\"mailSmtpEhlo\":null,\"mailSmtpAuth\":null,\"mailSmtpDnsNotify\":null,\"mailSmtpDnsRet\":null,\"mailSmtpAllow8bitmime\":null,\"mailSmtpSendPartial\":null,\"emailTemplateDirectory\":null,\"emailTemplateLogoImage\":null,\"emailSubjectLine\":null,\"id\":null}";
    }

    @Override
    public String getEntityJson() {
        return "{\"groupName\":\"EmailGroup\",\"id\":1}";
    }

    @Override
    public String getEmptyEntityJson() {
        return "{\"groupName\":null,\"id\":null}";
    }

}
