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
package com.synopsys.integration.alert.common.digest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.entity.NotificationCategoryEnum;

public class ProjectDataBuilder {
    private FrequencyType frequencyType;
    private String projectName;
    private String projectVersion;

    private final List<Long> notificationIds;

    private final Map<NotificationCategoryEnum, CategoryDataBuilder> categoryBuilderMap;

    public ProjectDataBuilder() {
        notificationIds = new ArrayList<>();
        categoryBuilderMap = new TreeMap<>();
    }

    public void addNotificationId(final Long notificationId) {
        notificationIds.add(notificationId);
    }

    public void addCategoryBuilder(final NotificationCategoryEnum category, final CategoryDataBuilder categoryBuilder) {
        categoryBuilderMap.put(category, categoryBuilder);
    }

    public void removeCategoryBuilder(final NotificationCategoryEnum category) {
        categoryBuilderMap.remove(category);
    }

    public Map<NotificationCategoryEnum, CategoryDataBuilder> getCategoryBuilderMap() {
        return categoryBuilderMap;
    }

    public FrequencyType getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(final FrequencyType frequencyType) {
        this.frequencyType = frequencyType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(final String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public ProjectData build() {
        final Map<NotificationCategoryEnum, CategoryData> categoryMap = new TreeMap<>();
        for (final Map.Entry<NotificationCategoryEnum, CategoryDataBuilder> entry : categoryBuilderMap.entrySet()) {
            categoryMap.put(entry.getKey(), entry.getValue().build());
        }
        return new ProjectData(frequencyType, projectName, projectVersion, notificationIds, categoryMap);
    }
}
