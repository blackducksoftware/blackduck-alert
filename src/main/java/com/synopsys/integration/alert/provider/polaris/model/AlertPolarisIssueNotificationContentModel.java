/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.polaris.model;

import org.apache.commons.lang3.EnumUtils;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class AlertPolarisIssueNotificationContentModel extends AlertSerializableModel {
    private final String notificationType;
    private final String projectName;
    private final String projectLink;
    private final String issueType;
    private final Integer count;
    // TODO packages?

    public AlertPolarisIssueNotificationContentModel(final AlertPolarisNotificationTypeEnum notificationType, final String projectName, final String projectLink, final String issueType, final Integer count) {
        this.notificationType = notificationType.name();
        this.projectName = projectName;
        this.projectLink = projectLink;
        this.issueType = issueType;
        this.count = count;
    }

    public AlertPolarisNotificationTypeEnum getNotificationType() {
        return EnumUtils.getEnum(AlertPolarisNotificationTypeEnum.class, notificationType);
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectLink() {
        return projectLink;
    }

    public String getIssueType() {
        return issueType;
    }

    public Integer getCount() {
        return count;
    }

}
