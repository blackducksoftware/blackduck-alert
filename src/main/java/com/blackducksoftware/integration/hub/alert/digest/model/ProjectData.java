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
package com.blackducksoftware.integration.hub.alert.digest.model;

import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;

public class ProjectData extends DigestData {
    private final DigestTypeEnum digestType;
    private final String projectKey;
    private final String projectName;
    private final String projectVersion;
    private final List<Long> notificationIds;
    private final Map<NotificationCategoryEnum, CategoryData> categoryMap;

    public ProjectData(final DigestTypeEnum digestType, final String projectName, final String projectVersion, final List<Long> notificationIds, final Map<NotificationCategoryEnum, CategoryData> categoryMap) {
        this.digestType = digestType;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.notificationIds = notificationIds;
        this.categoryMap = categoryMap;
        if (projectName != null && projectVersion != null) {
            this.projectKey = projectName + projectVersion;
        } else if (projectName == null) {
            this.projectKey = projectVersion;
        } else {
            this.projectKey = projectName;
        }
    }

    public DigestTypeEnum getDigestType() {
        return digestType;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public List<Long> getNotificationIds() {
        return notificationIds;
    }

    public Map<NotificationCategoryEnum, CategoryData> getCategoryMap() {
        return categoryMap;
    }

}
