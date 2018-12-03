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
import java.util.Set;

import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

public class EmailDistributionConfig extends CommonDistributionConfig {
    private String emailTemplateLogoImage;
    private String emailSubjectLine;
    private boolean projectOwnerOnly;
    private Set<String> emailAddresses;

    public EmailDistributionConfig() {
    }

    public EmailDistributionConfig(final String id, final String distributionConfigId, final String distributionType, final String name, final String providerName, final String frequency, final String filterByProject,
        final String emailTemplateLogoImage, final String emailSubjectLine, final String projectNamePattern, final boolean projectOwnerOnly, final List<String> configuredProjects, final List<String> notificationTypes,
        final String formatType, final Set<String> emailAddresses) {
        super(id, distributionConfigId, distributionType, name, providerName, frequency, filterByProject, projectNamePattern, configuredProjects, notificationTypes, formatType);
        this.emailTemplateLogoImage = emailTemplateLogoImage;
        this.emailSubjectLine = emailSubjectLine;
        this.projectOwnerOnly = projectOwnerOnly;
        this.emailAddresses = emailAddresses;
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

    public boolean getProjectOwnerOnly() {
        return projectOwnerOnly;
    }

    public void setProjectOwnerOnly(final boolean projectOwnerOnly) {
        this.projectOwnerOnly = projectOwnerOnly;
    }

    public Set<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(final Set<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }
}
