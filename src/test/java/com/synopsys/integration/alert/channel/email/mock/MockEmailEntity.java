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
package com.synopsys.integration.alert.channel.email.mock;

import java.util.Collections;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

public class MockEmailEntity extends MockEntityUtil<EmailGroupDistributionConfigEntity> {
    private final String emailTemplateLogoImage;
    private final String emailSubjectLine;
    private String groupName;
    private Long id;

    public MockEmailEntity() {
        this("groupName", 1L, "emailTemplateLogoImage", "emailSubjectLine");
    }

    private MockEmailEntity(final String groupName, final Long id, final String emailTemplateLogoImage, final String emailSubjectLine) {
        super();
        this.groupName = groupName;
        this.id = id;
        this.emailTemplateLogoImage = emailTemplateLogoImage;
        this.emailSubjectLine = emailSubjectLine;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public String getEmailTemplateLogoImage() {
        return emailTemplateLogoImage;
    }

    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Config createConfig() {
        return new EmailDistributionConfig(id.toString(), groupName, emailTemplateLogoImage, emailSubjectLine, "0L", EmailGroupChannel.COMPONENT_NAME, "EmailTest", BlackDuckProvider.COMPONENT_NAME, "real_time", "false",
            Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public Config createEmptyConfig() {
        return new EmailDistributionConfig();
    }

    @Override
    public EmailGroupDistributionConfigEntity createEntity() {
        final EmailGroupDistributionConfigEntity entity = new EmailGroupDistributionConfigEntity(groupName, emailTemplateLogoImage, emailSubjectLine);
        entity.setId(id);
        return entity;
    }

    @Override
    public EmailGroupDistributionConfigEntity createEmptyEntity() {
        return new EmailGroupDistributionConfigEntity();
    }

    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("groupName", groupName);
        json.addProperty("id", Long.valueOf(id));
        json.addProperty("emailTemplateLogoImage", emailTemplateLogoImage);
        json.addProperty("emailSubjectLine", emailSubjectLine);
        return json.toString();
    }

}
