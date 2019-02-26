/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.provider.polaris.model;

import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class AlertPolarisIssueNotificationContentModel extends AlertSerializableModel {
    private final String notificationType;
    private final String description;
    private final String localEffect;
    private final String owner;
    private final String dismissalStatus;
    private final String issueStatus;
    private final String link;

    public AlertPolarisIssueNotificationContentModel(final AlertPolarisNotificationTypeEnum notificationType, final String description, final String localEffect, final String owner, final String dismissalStatus, final String issueStatus,
        final String link) {
        this.notificationType = notificationType.name();
        this.description = description;
        this.localEffect = localEffect;
        this.owner = owner;
        this.dismissalStatus = dismissalStatus;
        this.issueStatus = issueStatus;
        this.link = link;
    }

    public AlertPolarisIssueNotificationContentModel(final AlertPolarisNotificationTypeEnum notificationType, final String description, final String localEffect, final String owner, final String dismissalStatus, final String issueStatus) {
        this.notificationType = notificationType.name();
        this.description = description;
        this.localEffect = localEffect;
        this.owner = owner;
        this.dismissalStatus = dismissalStatus;
        this.issueStatus = issueStatus;
        this.link = null;
    }

    public AlertPolarisNotificationTypeEnum getNotificationType() {
        return EnumUtils.getEnum(AlertPolarisNotificationTypeEnum.class, notificationType);
    }

    public String getDescription() {
        return description;
    }

    public String getLocalEffect() {
        return localEffect;
    }

    public Optional<String> getLink() {
        return Optional.ofNullable(link);
    }

    public String getOwner() {
        return owner;
    }

    public String getDismissalStatus() {
        return dismissalStatus;
    }

    public String getIssueStatus() {
        return issueStatus;
    }
}
