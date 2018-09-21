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

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;

public class MockEmailEntity extends MockEntityUtil<EmailGroupDistributionConfigEntity> {
    private final String emailTemplateLogoImage;
    private final String emailSubjectLine;
    private final boolean projectOwnerOnly;
    private Long id;

    public MockEmailEntity() {
        this(1L, "emailTemplateLogoImage", "emailSubjectLine", false);
    }

    private MockEmailEntity(final Long id, final String emailTemplateLogoImage, final String emailSubjectLine, boolean projectOwnerOnly) {
        super();
        this.id = id;
        this.emailTemplateLogoImage = emailTemplateLogoImage;
        this.emailSubjectLine = emailSubjectLine;
        this.projectOwnerOnly = projectOwnerOnly;
    }

    public String getEmailTemplateLogoImage() {
        return emailTemplateLogoImage;
    }

    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

    public boolean isProjectOwnerOnly() {
        return projectOwnerOnly;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public EmailGroupDistributionConfigEntity createEntity() {
        final EmailGroupDistributionConfigEntity entity = new EmailGroupDistributionConfigEntity(emailTemplateLogoImage, emailSubjectLine, projectOwnerOnly);
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
        json.addProperty("id", Long.valueOf(id));
        json.addProperty("emailTemplateLogoImage", emailTemplateLogoImage);
        json.addProperty("emailSubjectLine", emailSubjectLine);
        json.addProperty("projectOwnerOnly", projectOwnerOnly);
        return json.toString();
    }

}
