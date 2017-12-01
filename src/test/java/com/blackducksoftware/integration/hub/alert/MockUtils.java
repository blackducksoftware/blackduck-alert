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
package com.blackducksoftware.integration.hub.alert;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.HubUsersConfigRestModel;

public class MockUtils {

    public GlobalProperties createTestGlobalProperties(final GlobalHubRepository globalRepository) {
        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository);
        globalProperties.hubUrl = "HubUrl";
        globalProperties.hubTrustCertificate = false;
        globalProperties.hubProxyHost = "HubProxyHost";
        globalProperties.hubProxyPort = "22";
        globalProperties.hubProxyUsername = "HubProxyUsername";
        globalProperties.hubProxyPassword = "HubProxyPassword";
        return globalProperties;
    }

    public String getGlobalConfigRestModelJson() {
        return "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":\"11\",\"hubUsername\":\"HubUsername\",\"hubPassword\":\"HubPassword\",\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"22\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubProxyPassword\":\"HubProxyPassword\",\"hubAlwaysTrustCertificate\":\"false\",\"accumulatorCron\":\"0 0/1 * 1/1 * *\",\"dailyDigestCron\":\"0 0/1 * 1/1 * *\",\"id\":\"1\"}";
    }

    public String getGlobalConfigEntityJson() {
        return "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":11,\"hubUsername\":\"HubUsername\",\"hubPassword\":\"HubPassword\",\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"22\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubProxyPassword\":\"HubProxyPassword\",\"hubAlwaysTrustCertificate\":false,\"accumulatorCron\":\"0 0/1 * 1/1 * *\",\"dailyDigestCron\":\"0 0/1 * 1/1 * *\",\"id\":1}";
    }

    public GlobalConfigRestModel createGlobalConfigRestModel() {
        final GlobalConfigRestModel restModel = new GlobalConfigRestModel("1", "HubUrl", "11", "HubUsername", "HubPassword", "HubProxyHost", "22", "HubProxyUsername", "HubProxyPassword", "false", "0 0/1 * 1/1 * *", "0 0/1 * 1/1 * *",
                "0 0 12 1/2 * *");
        return restModel;
    }

    public GlobalConfigRestModel createGlobalConfigMaskedRestModel() {
        final GlobalConfigRestModel restModel = new GlobalConfigRestModel("1", "HubUrl", "11", "HubUsername", null, "HubProxyHost", "22", "HubProxyUsername", null, "false", "0 0/1 * 1/1 * *", "0 0/1 * 1/1 * *", "0 0 12 1/2 * *");
        return restModel;
    }

    public GlobalHubConfigEntity createGlobalConfigEntity() {
        final GlobalHubConfigEntity configEntity = new GlobalHubConfigEntity(11, "HubUsername", "HubPassword", "0 0/1 * 1/1 * *", "0 0/1 * 1/1 * *", "0 0 12 1/2 * *");
        configEntity.setId(1L);
        return configEntity;
    }

    public String getEmailConfigRestModelJson() {
        return "{\"mailSmtpHost\":\"MailSmtpHost\",\"mailSmtpUser\":\"MailSmtpUser\",\"mailSmtpPassword\":\"MailSmtpPassword\",\"mailSmtpPort\":\"33\",\"mailSmtpConnectionTimeout\":\"11\",\"mailSmtpTimeout\":\"22\",\"mailSmtpFrom\":\"MailSmtpFrom\",\"mailSmtpLocalhost\":\"MailSmtpLocalhost\",\"mailSmtpEhlo\":\"false\",\"mailSmtpAuth\":\"true\",\"mailSmtpDnsNotify\":\"MailSmtpDnsNotify\",\"mailSmtpDsnRet\":\"MailSmtpDnsRet\",\"mailSmtpAllow8bitmime\":\"false\",\"mailSmtpSendPartial\":\"false\",\"emailTemplateDirectory\":\"MailSmtpTemplateDirectory\",\"emailTemplateLogoImage\":\"MailSmtpTemplateLogoImage\",\"emailSubjectLine\":\"MailSmtpSubjectLine\",\"id\":\"1\"}";
    }

    public String getEmailConfigEntityJson() {
        return "{\"mailSmtpHost\":\"MailSmtpHost\",\"mailSmtpUser\":\"MailSmtpUser\",\"mailSmtpPassword\":\"MailSmtpPassword\",\"mailSmtpPort\":33,\"mailSmtpConnectionTimeout\":11,\"mailSmtpTimeout\":22,\"mailSmtpFrom\":\"MailSmtpFrom\",\"mailSmtpLocalhost\":\"MailSmtpLocalhost\",\"mailSmtpEhlo\":false,\"mailSmtpAuth\":true,\"mailSmtpDnsNotify\":\"MailSmtpDnsNotify\",\"mailSmtpDsnRet\":\"MailSmtpDnsRet\",\"mailSmtpAllow8bitmime\":false,\"mailSmtpSendPartial\":false,\"emailTemplateDirectory\":\"MailSmtpTemplateDirectory\",\"emailTemplateLogoImage\":\"MailSmtpTemplateLogoImage\",\"emailSubjectLine\":\"MailSmtpSubjectLine\",\"id\":1}";
    }

    public EmailConfigRestModel createEmailConfigRestModel() {
        final EmailConfigRestModel restModel = new EmailConfigRestModel("1", "MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", "33", "11", "22", "MailSmtpFrom", "MailSmtpLocalhost", "false", "true", "MailSmtpDnsNotify", "MailSmtpDnsRet",
                "false", "false", "MailSmtpTemplateDirectory", "MailSmtpTemplateLogoImage", "MailSmtpSubjectLine");
        return restModel;
    }

    public EmailConfigRestModel createEmailConfigMaskedRestModel() {
        final EmailConfigRestModel restModel = new EmailConfigRestModel("1", "MailSmtpHost", "MailSmtpUser", null, "33", "11", "22", "MailSmtpFrom", "MailSmtpLocalhost", "false", "true", "MailSmtpDnsNotify", "MailSmtpDnsRet", "false",
                "false", "MailSmtpTemplateDirectory", "MailSmtpTemplateLogoImage", "MailSmtpSubjectLine");
        return restModel;
    }

    public GlobalEmailConfigEntity createEmailConfigEntity() {
        final GlobalEmailConfigEntity configEntity = new GlobalEmailConfigEntity("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", 33, 11, 22, "MailSmtpFrom", "MailSmtpLocalhost", false, true, "MailSmtpDnsNotify", "MailSmtpDnsRet", false,
                false, "MailSmtpTemplateDirectory", "MailSmtpTemplateLogoImage", "MailSmtpSubjectLine");
        configEntity.setId(1L);
        return configEntity;
    }

    public String getHipChatConfigRestModelJson() {
        return "{\"apiKey\":\"ApiKey\",\"roomId\":\"11\",\"notify\":\"false\",\"color\":\"black\",\"id\":\"1\"}";
    }

    public String getHipChatConfigEntityJson() {
        return "{\"apiKey\":\"ApiKey\",\"roomId\":11,\"notify\":false,\"color\":\"black\",\"id\":1}";
    }

    public HipChatConfigRestModel createHipChatConfigRestModel() {
        final HipChatConfigRestModel restModel = new HipChatConfigRestModel("1", "ApiKey", "11", "false", "black");
        return restModel;
    }

    public GlobalHipChatConfigEntity createHipChatConfigEntity() {
        final GlobalHipChatConfigEntity configEntity = new GlobalHipChatConfigEntity("ApiKey", 11, false, "black");
        configEntity.setId(1L);
        return configEntity;
    }

    public HubUsersConfigRestModel createHubUsersRestModel() {
        return createHubUsersRestModel("user");
    }

    public HubUsersConfigRestModel createHubUsersRestModel(final String username) {
        return new HubUsersConfigRestModel("1", username);
    }

}
