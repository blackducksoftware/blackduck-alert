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
package com.blackducksoftware.integration.hub.alert.web;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;

public class ObjectTransformerTest {

    @Test
    public void transformGlobalModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalConfigRestModel restModel = createGlobalConfigRestModel();
        final GlobalConfigEntity configEntity = createGlobalConfigEntity();

        final GlobalConfigEntity transformedConfigEntity = objectTransformer.tranformObject(restModel, GlobalConfigEntity.class);
        final GlobalConfigRestModel transformedConfigRestModel = objectTransformer.tranformObject(configEntity, GlobalConfigRestModel.class);
        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

    @Test
    public void transformEmailModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final EmailConfigRestModel restModel = createEmailConfigRestModel();
        final EmailConfigEntity configEntity = createEmailConfigEntity();

        final EmailConfigEntity transformedConfigEntity = objectTransformer.tranformObject(restModel, EmailConfigEntity.class);
        final EmailConfigRestModel transformedConfigRestModel = objectTransformer.tranformObject(configEntity, EmailConfigRestModel.class);
        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

    @Test
    public void transformHipchatModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final HipChatConfigRestModel restModel = createHipChatConfigRestModel();
        final HipChatConfigEntity configEntity = createHipChatConfigEntity();

        final HipChatConfigEntity transformedConfigEntity = objectTransformer.tranformObject(restModel, HipChatConfigEntity.class);
        final HipChatConfigRestModel transformedConfigRestModel = objectTransformer.tranformObject(configEntity, HipChatConfigRestModel.class);
        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

    private GlobalConfigRestModel createGlobalConfigRestModel() {
        final GlobalConfigRestModel restModel = new GlobalConfigRestModel("1", "HubUrl", "11", "HubUsername", "HubPassword", "HubProxyHost", "22", "HubProxyUsername", "HubProxyPassword", "false", "0 0/1 * 1/1 * *", "0 0/1 * 1/1 * *");
        return restModel;
    }

    private GlobalConfigEntity createGlobalConfigEntity() {
        final GlobalConfigEntity configEntity = new GlobalConfigEntity("HubUrl", 11, "HubUsername", "HubPassword", "HubProxyHost", "22", "HubProxyUsername", "HubProxyPassword", false, "0 0/1 * 1/1 * *", "0 0/1 * 1/1 * *");
        configEntity.setId(1L);
        return configEntity;
    }

    private EmailConfigRestModel createEmailConfigRestModel() {
        final EmailConfigRestModel restModel = new EmailConfigRestModel("1", "MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", "33", "11", "22", "MailSmtpFrom", "MailSmtpLocalhost", "false", "true", "MailSmtpDnsNotify", "MailSmtpDnsRet",
                "false", "false", "MailSmtpTemplateDirectory", "MailSmtpTemplateLogoImage", "MailSmtpSubjectLine");
        return restModel;
    }

    private EmailConfigEntity createEmailConfigEntity() {
        final EmailConfigEntity configEntity = new EmailConfigEntity("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", 33, 11, 22, "MailSmtpFrom", "MailSmtpLocalhost", false, true, "MailSmtpDnsNotify", "MailSmtpDnsRet", false, false,
                "MailSmtpTemplateDirectory", "MailSmtpTemplateLogoImage", "MailSmtpSubjectLine");
        configEntity.setId(1L);
        return configEntity;
    }

    private HipChatConfigRestModel createHipChatConfigRestModel() {
        final HipChatConfigRestModel restModel = new HipChatConfigRestModel("1", "ApiKey", "11", "false", "black");
        return restModel;
    }

    private HipChatConfigEntity createHipChatConfigEntity() {
        final HipChatConfigEntity configEntity = new HipChatConfigEntity("ApiKey", 11, false, "black");
        configEntity.setId(1L);
        return configEntity;
    }

}
