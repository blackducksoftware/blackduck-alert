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
package com.synopsys.integration.alert.common.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.synopsys.integration.alert.database.entity.NotificationCategoryEnum;
import com.synopsys.integration.alert.database.entity.NotificationEntity;
import com.synopsys.integration.alert.database.entity.VulnerabilityEntity;

public class NotificationModel {
    private final NotificationEntity notification;
    private final Collection<VulnerabilityEntity> vulnerabilities;

    public NotificationModel(final NotificationEntity notification, final Collection<VulnerabilityEntity> vulnerabilities) {
        this.notification = notification;
        this.vulnerabilities = vulnerabilities;
    }

    public NotificationEntity getNotificationEntity() {
        return notification;
    }

    public String getEventKey() {
        if (notification != null) {
            return notification.getEventKey();
        } else {
            return null;
        }
    }

    public Date getCreatedAt() {
        if (notification != null) {
            return notification.getCreatedAt();
        } else {
            return null;
        }
    }

    public NotificationCategoryEnum getNotificationType() {
        if (notification != null) {
            return notification.getNotificationType();
        } else {
            return null;
        }
    }

    public String getProjectName() {
        if (notification != null) {
            return notification.getProjectName();
        } else {
            return null;
        }
    }

    public String getProjectVersion() {
        if (notification != null) {
            return notification.getProjectVersion();
        } else {
            return null;
        }
    }

    public String getComponentName() {
        if (notification != null) {
            return notification.getComponentName();
        } else {
            return null;
        }
    }

    public String getComponentVersion() {
        if (notification != null) {
            return notification.getComponentVersion();
        } else {
            return null;
        }
    }

    public String getPolicyRuleName() {
        if (notification != null) {
            return notification.getPolicyRuleName();
        } else {
            return null;
        }
    }

    public String getPolicyRuleUser() {
        if (notification != null) {
            return notification.getPolicyRuleUser();
        } else {
            return null;
        }
    }

    public String getProjectUrl() {
        if (notification != null) {
            return notification.getProjectUrl();
        } else {
            return null;
        }
    }

    public String getProjectVersionUrl() {
        if (notification != null) {
            return notification.getProjectVersionUrl();
        } else {
            return null;
        }
    }

    public Collection<VulnerabilityEntity> getVulnerabilityList() {
        if (vulnerabilities == null) {
            return Collections.emptyList();
        } else {
            return vulnerabilities;
        }
    }
}
