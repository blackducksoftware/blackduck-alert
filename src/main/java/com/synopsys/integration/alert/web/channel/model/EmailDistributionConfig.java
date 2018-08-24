/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.channel.model;

import java.util.List;

import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

public class EmailDistributionConfig extends CommonDistributionConfig {
    private String groupName;
    private String emailTemplateLogoImage;
    private String emailSubjectLine;

    public EmailDistributionConfig() {
    }

    public EmailDistributionConfig(final String id, final String distributionConfigId, final String distributionType, final String name, final String providerName, final String frequency, final String filterByProject,
            final String groupName,
            final String emailTemplateLogoImage, final String emailSubjectLine, final List<String> configuredProjects, final List<String> notificationTypes) {
        super(id, distributionConfigId, distributionType, name, providerName, frequency, filterByProject, configuredProjects, notificationTypes);
        this.groupName = groupName;
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

    public void setEmailTemplateLogoImage(final String emailTemplateLogoImage) {
        this.emailTemplateLogoImage = emailTemplateLogoImage;
    }

    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

    public void setEmailSubjectLine(final String emailSubjectLine) {
        this.emailSubjectLine = emailSubjectLine;
    }

}
