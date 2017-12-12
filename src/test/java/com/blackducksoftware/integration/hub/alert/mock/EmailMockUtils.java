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

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalEmailConfigRestModel;

public class EmailMockUtils {

    public String getGlobalEmailConfigRestModelJson() {
        return "{\"mailSmtpHost\":\"MailSmtpHost\",\"mailSmtpUser\":\"MailSmtpUser\",\"mailSmtpPassword\":\"MailSmtpPassword\",\"mailSmtpPort\":\"33\",\"mailSmtpConnectionTimeout\":\"11\",\"mailSmtpTimeout\":\"22\",\"mailSmtpFrom\":\"MailSmtpFrom\",\"mailSmtpLocalhost\":\"MailSmtpLocalhost\",\"mailSmtpEhlo\":\"false\",\"mailSmtpAuth\":\"true\",\"mailSmtpDnsNotify\":\"MailSmtpDnsNotify\",\"mailSmtpDsnRet\":\"MailSmtpDnsRet\",\"mailSmtpAllow8bitmime\":\"false\",\"mailSmtpSendPartial\":\"false\",\"emailTemplateDirectory\":\"EmailTemplateDirectory\",\"emailTemplateLogoImage\":\"EmailTemplateLogoImage\",\"emailSubjectLine\":\"EmailSubjectLine\",\"id\":\"1\"}";
    }

    public String getEmptyGlobalEmailConfigRestModelJson() {
        return "{\"mailSmtpHost\":null,\"mailSmtpUser\":null,\"mailSmtpPort\":null,\"mailSmtpConnectionTimeout\":null,\"mailSmtpTimeout\":null,\"mailSmtpFrom\":null,\"mailSmtpLocalhost\":null,\"mailSmtpEhlo\":null,\"mailSmtpAuth\":null,\"mailSmtpDnsNotify\":null,\"mailSmtpDnsRet\":null,\"mailSmtpAllow8bitmime\":null,\"mailSmtpSendPartial\":null,\"emailTemplateDirectory\":null,\"emailTemplateLogoImage\":null,\"emailSubjectLine\":null,\"id\":null}";

    }

    public String getGlobalEmailConfigEntityJson() {
        return "{\"mailSmtpHost\":\"MailSmtpHost\",\"mailSmtpUser\":\"MailSmtpUser\",\"mailSmtpPassword\":\"MailSmtpPassword\",\"mailSmtpPort\":33,\"mailSmtpConnectionTimeout\":11,\"mailSmtpTimeout\":22,\"mailSmtpFrom\":\"MailSmtpFrom\",\"mailSmtpLocalhost\":\"MailSmtpLocalhost\",\"mailSmtpEhlo\":false,\"mailSmtpAuth\":true,\"mailSmtpDnsNotify\":\"MailSmtpDnsNotify\",\"mailSmtpDsnRet\":\"MailSmtpDnsRet\",\"mailSmtpAllow8bitmime\":false,\"mailSmtpSendPartial\":false,\"emailTemplateDirectory\":\"EmailTemplateDirectory\",\"emailTemplateLogoImage\":\"EmailTemplateLogoImage\",\"emailSubjectLine\":\"EmailSubjectLine\",\"id\":1}";
    }

    public GlobalEmailConfigRestModel createGlobalEmailConfigRestModel() {
        final GlobalEmailConfigRestModel restModel = new GlobalEmailConfigRestModel("1", "MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", "33", "11", "22", "MailSmtpFrom", "MailSmtpLocalhost", "false", "true", "MailSmtpDnsNotify",
                "MailSmtpDnsRet", "false", "false", "EmailTemplateDirectory", "EmailTemplateLogoImage", "EmailSubjectLine");
        return restModel;
    }

    public GlobalEmailConfigRestModel createGlobalEmailConfigMaskedRestModel() {
        final GlobalEmailConfigRestModel restModel = new GlobalEmailConfigRestModel("1", "MailSmtpHost", "MailSmtpUser", null, "33", "11", "22", "MailSmtpFrom", "MailSmtpLocalhost", "false", "true", "MailSmtpDnsNotify", "MailSmtpDnsRet",
                "false", "false", "EmailTemplateDirectory", "EmailTemplateLogoImage", "EmailSubjectLine");
        return restModel;
    }

    public GlobalEmailConfigEntity createGlobalEmailConfigEntity() {
        final GlobalEmailConfigEntity configEntity = new GlobalEmailConfigEntity("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", 33, 11, 22, "MailSmtpFrom", "MailSmtpLocalhost", false, true, "MailSmtpDnsNotify", "MailSmtpDnsRet", false,
                false, "EmailTemplateDirectory", "EmailTemplateLogoImage", "EmailSubjectLine");
        configEntity.setId(1L);
        return configEntity;
    }
}
