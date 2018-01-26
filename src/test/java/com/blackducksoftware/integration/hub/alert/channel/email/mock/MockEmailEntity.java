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

import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.google.gson.JsonObject;

public class MockEmailEntity extends MockEntityUtil<EmailGroupDistributionConfigEntity> {
    private String groupName;
    private Long id;
    private final String emailTemplateLogoImage;
    private final String emailSubjectLine;

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

    public String getEmailTemplateLogoImage() {
        return emailTemplateLogoImage;
    }

    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
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
