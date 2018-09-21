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
import com.synopsys.integration.alert.mock.model.MockCommonDistributionRestModel;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;

public class MockEmailRestModel extends MockRestModelUtil<EmailDistributionConfig> {
    private final MockCommonDistributionRestModel distributionMockUtil = new MockCommonDistributionRestModel();

    private String id;
    private String emailTemplateLogoImage;
    private String emailSubjectLine;

    public MockEmailRestModel() {
        this("1", "emailTemplateLogoImage", "emailSubjectLine");
    }

    private MockEmailRestModel(final String id, final String emailTemplateLogoImage, final String emailSubjectLine) {
        this.id = id;
        this.emailTemplateLogoImage = emailTemplateLogoImage;
        this.emailSubjectLine = emailSubjectLine;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setEmailTemplateLogoImage(final String emailTemplateLogoImage) {
        this.emailTemplateLogoImage = emailTemplateLogoImage;
    }

    public void setEmailSubjectLine(final String emailSubjectLine) {
        this.emailSubjectLine = emailSubjectLine;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    public String getEmailTemplateLogoImage() {
        return emailTemplateLogoImage;
    }

    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

    @Override
    public EmailDistributionConfig createRestModel() {
        final EmailDistributionConfig restModel = new EmailDistributionConfig(String.valueOf(distributionMockUtil.getId()), distributionMockUtil.getDistributionConfigId(), distributionMockUtil.getDistributionType(),
            distributionMockUtil.getName(), distributionMockUtil.getProviderName(), distributionMockUtil.getFrequency(), distributionMockUtil.getFilterByProject(), emailTemplateLogoImage, emailSubjectLine,
            distributionMockUtil.getProjects(),
            distributionMockUtil.getNotificationsAsStrings(), distributionMockUtil.getFormatType());
        return restModel;
    }

    @Override
    public EmailDistributionConfig createEmptyRestModel() {
        return new EmailDistributionConfig();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("emailTemplateLogoImage", emailTemplateLogoImage);
        json.addProperty("emailSubjectLine", emailSubjectLine);

        return distributionMockUtil.combineWithRestModelJson(json);
    }

}
