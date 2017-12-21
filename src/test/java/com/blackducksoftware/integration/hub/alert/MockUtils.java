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

import java.util.Collections;
import java.util.Date;

import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHipChatConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalSchedulingConfigRestModel;

public class MockUtils {

    public GlobalProperties createTestGlobalProperties(final GlobalHubRepository globalRepository, final GlobalSchedulingRepository globalSchedulingRepository) {
        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository, globalSchedulingRepository);
        globalProperties.setHubUrl("HubUrl");
        globalProperties.setHubTrustCertificate(false);
        globalProperties.setHubProxyHost("HubProxyHost");
        globalProperties.setHubProxyPort("22");
        globalProperties.setHubProxyUsername("HubProxyUsername");
        globalProperties.setHubProxyPassword("HubProxyPassword");
        return globalProperties;
    }

    public String getGlobalConfigRestModelJson() {
        return "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":\"11\",\"hubUsername\":\"HubUsername\",\"hubPassword\":\"HubPassword\",\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"22\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubProxyPassword\":\"HubProxyPassword\",\"hubAlwaysTrustCertificate\":\"false\",\"accumulatorCron\":\"0 0/1 * 1/1 * *\",\"dailyDigestCron\":\"0 0/1 * 1/1 * *\",\"id\":\"1\"}";
    }

    public String getGlobalConfigEntityJson() {
        return "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":11,\"hubUsername\":\"HubUsername\",\"hubPassword\":\"HubPassword\",\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"22\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubProxyPassword\":\"HubProxyPassword\",\"hubAlwaysTrustCertificate\":false,\"accumulatorCron\":\"0 0/1 * 1/1 * *\",\"dailyDigestCron\":\"0 0/1 * 1/1 * *\",\"id\":1}";
    }

    public GlobalHubConfigRestModel createGlobalHubConfigRestModel() {
        final GlobalHubConfigRestModel restModel = new GlobalHubConfigRestModel("1", "HubUrl", "11", "HubUsername", "HubPassword", "HubProxyHost", "22", "HubProxyUsername", "HubProxyPassword", "false");
        return restModel;
    }

    public GlobalHubConfigRestModel createGlobalHubConfigMaskedRestModel() {
        final GlobalHubConfigRestModel restModel = new GlobalHubConfigRestModel("1", "HubUrl", "11", "HubUsername", null, "HubProxyHost", "22", "HubProxyUsername", null, "false");
        return restModel;
    }

    public GlobalHubConfigEntity createGlobalHubConfigEntity() {
        final GlobalHubConfigEntity configEntity = new GlobalHubConfigEntity(11, "HubUsername", "HubPassword");
        configEntity.setId(1L);
        return configEntity;
    }

    public GlobalSchedulingConfigRestModel createGlobalSchedulingConfigRestModel() {
        return new GlobalSchedulingConfigRestModel("1", "0 0/1 * 1/1 * ?", "0 0/1 * 1/1 * ?", "0 0/1 * 1/1 * ?");
    }

    public GlobalSchedulingConfigEntity createGlobalSchedulingConfigEntity() {
        return new GlobalSchedulingConfigEntity("0 0/1 * 1/1 * ?", "0 0/1 * 1/1 * ?", "0 0/1 * 1/1 * ?");
    }

    public String getEmailConfigRestModelJson() {
        return "{\"mailSmtpHost\":\"MailSmtpHost\",\"mailSmtpUser\":\"MailSmtpUser\",\"mailSmtpPassword\":\"MailSmtpPassword\",\"mailSmtpPort\":\"33\",\"mailSmtpConnectionTimeout\":\"11\",\"mailSmtpTimeout\":\"22\",\"mailSmtpFrom\":\"MailSmtpFrom\",\"mailSmtpLocalhost\":\"MailSmtpLocalhost\",\"mailSmtpEhlo\":\"false\",\"mailSmtpAuth\":\"true\",\"mailSmtpDnsNotify\":\"MailSmtpDnsNotify\",\"mailSmtpDsnRet\":\"MailSmtpDnsRet\",\"mailSmtpAllow8bitmime\":\"false\",\"mailSmtpSendPartial\":\"false\",\"emailTemplateDirectory\":\"MailSmtpTemplateDirectory\",\"emailTemplateLogoImage\":\"MailSmtpTemplateLogoImage\",\"emailSubjectLine\":\"MailSmtpSubjectLine\",\"id\":\"1\"}";
    }

    public String getEmailConfigEntityJson() {
        return "{\"mailSmtpHost\":\"MailSmtpHost\",\"mailSmtpUser\":\"MailSmtpUser\",\"mailSmtpPassword\":\"MailSmtpPassword\",\"mailSmtpPort\":33,\"mailSmtpConnectionTimeout\":11,\"mailSmtpTimeout\":22,\"mailSmtpFrom\":\"MailSmtpFrom\",\"mailSmtpLocalhost\":\"MailSmtpLocalhost\",\"mailSmtpEhlo\":false,\"mailSmtpAuth\":true,\"mailSmtpDnsNotify\":\"MailSmtpDnsNotify\",\"mailSmtpDsnRet\":\"MailSmtpDnsRet\",\"mailSmtpAllow8bitmime\":false,\"mailSmtpSendPartial\":false,\"emailTemplateDirectory\":\"MailSmtpTemplateDirectory\",\"emailTemplateLogoImage\":\"MailSmtpTemplateLogoImage\",\"emailSubjectLine\":\"MailSmtpSubjectLine\",\"id\":1}";
    }

    public GlobalEmailConfigRestModel createEmailConfigRestModel() {
        final GlobalEmailConfigRestModel restModel = new GlobalEmailConfigRestModel("1", "MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", "33", "11", "22", "MailSmtpFrom", "MailSmtpLocalhost", "false", "true", "MailSmtpDnsNotify",
                "MailSmtpDnsRet", "false", "false", "MailSmtpTemplateDirectory", "MailSmtpTemplateLogoImage", "MailSmtpSubjectLine");
        return restModel;
    }

    public GlobalEmailConfigRestModel createEmailConfigMaskedRestModel() {
        final GlobalEmailConfigRestModel restModel = new GlobalEmailConfigRestModel("1", "MailSmtpHost", "MailSmtpUser", null, "33", "11", "22", "MailSmtpFrom", "MailSmtpLocalhost", "false", "true", "MailSmtpDnsNotify", "MailSmtpDnsRet",
                "false", "false", "MailSmtpTemplateDirectory", "MailSmtpTemplateLogoImage", "MailSmtpSubjectLine");
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

    public GlobalHipChatConfigRestModel createHipChatConfigRestModel() {
        final GlobalHipChatConfigRestModel restModel = new GlobalHipChatConfigRestModel("1", "ApiKey");
        return restModel;
    }

    public GlobalHipChatConfigEntity createHipChatConfigEntity() {
        final GlobalHipChatConfigEntity configEntity = new GlobalHipChatConfigEntity("ApiKey");
        configEntity.setId(1L);
        return configEntity;
    }

    public CommonDistributionConfigEntity createCommonDistributionConfigEntity() {
        return new CommonDistributionConfigEntity(null, SupportedChannels.HIPCHAT, "Job Name", "REAL_TIME", Boolean.FALSE);
    }

    public NotificationEntity createNotificationEntity() {
        return new NotificationEntity("_event_key_", new Date(), "POLICY_VIOLATION", "Test Project Name", "", "Test Project Version Name", "", "Component Name", "Component Version Name", "Policy Rule Name", "Person",
                Collections.emptyList());
    }

}
