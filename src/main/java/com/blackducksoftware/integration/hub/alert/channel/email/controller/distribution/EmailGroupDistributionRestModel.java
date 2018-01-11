/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution;

import java.util.List;

import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public class EmailGroupDistributionRestModel extends CommonDistributionConfigRestModel {
    private static final long serialVersionUID = -788699253586880472L;

    private String groupName;
    private String emailTemplateLogoImage;
    private String emailSubjectLine;

    public EmailGroupDistributionRestModel() {
    }

    public EmailGroupDistributionRestModel(final String id, final String distributionConfigId, final String distributionType, final String name, final String frequency, final String filterByProject, final String groupName,
            final String emailTemplateLogoImage, final String emailSubjectLine, final List<String> configuredProjects, final List<String> notificationTypes) {
        super(id, distributionConfigId, distributionType, name, frequency, filterByProject, configuredProjects, notificationTypes);
        this.groupName = groupName;
        this.emailTemplateLogoImage = emailTemplateLogoImage;
        this.emailSubjectLine = emailSubjectLine;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
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
