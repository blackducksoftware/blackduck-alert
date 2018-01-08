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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.processor.VulnerabilityCache;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

@Component
public class ProjectDataFactory {

    public Collection<ProjectData> createProjectDataCollection(final Collection<NotificationEntity> notifications) {
        return createProjectDataCollection(notifications, DigestTypeEnum.REAL_TIME);
    }

    public Collection<ProjectData> createProjectDataCollection(final Collection<NotificationEntity> notifications, final DigestTypeEnum digestType) {
        final Map<String, ProjectDataBuilder> projectDataMap = new LinkedHashMap<>();
        for (final NotificationEntity entity : notifications) {
            final String projectKey = entity.getEventKey();

            ProjectDataBuilder projectBuilder;
            if (projectDataMap.containsKey(projectKey)) {
                projectBuilder = projectDataMap.get(projectKey);
            } else {
                projectBuilder = getProjectDataBuilder(entity, digestType);
                projectDataMap.put(projectKey, projectBuilder);
            }
            projectBuilder.addNotificationId(entity.getId());
            final CategoryDataBuilder categoryDataBuilder = getCategoryDataBuilder(entity, projectBuilder.getCategoryBuilderMap());
            addCategoryData(entity, categoryDataBuilder);
        }
        return collectProjectDataFromMap(projectDataMap);
    }

    public ProjectData createProjectData(final NotificationEntity notification) {
        return createProjectData(notification, DigestTypeEnum.REAL_TIME);
    }

    public ProjectData createProjectData(final NotificationEntity notification, final DigestTypeEnum digestType) {
        final ProjectDataBuilder projectBuilder = getProjectDataBuilder(notification, digestType);
        projectBuilder.addNotificationId(notification.getId());
        final CategoryDataBuilder categoryData = new CategoryDataBuilder();
        categoryData.setCategoryKey(getCategoryKey(notification).name());
        addCategoryData(notification, categoryData);
        return projectBuilder.build();
    }

    // get category map from the project or create the project data if it doesn't exist
    private ProjectDataBuilder getProjectDataBuilder(final NotificationEntity notification, final DigestTypeEnum digestType) {
        ProjectDataBuilder projectBuilder;
        projectBuilder = new ProjectDataBuilder();
        projectBuilder.setDigestType(digestType);
        projectBuilder.setProjectName(notification.getProjectName());
        projectBuilder.setProjectVersion(notification.getProjectVersion());
        return projectBuilder;
    }

    private CategoryDataBuilder getCategoryDataBuilder(final NotificationEntity notification, final Map<NotificationCategoryEnum, CategoryDataBuilder> categoryBuilderMap) {
        CategoryDataBuilder categoryData;
        final NotificationCategoryEnum categoryKey = getCategoryKey(notification);
        if (!categoryBuilderMap.containsKey(categoryKey)) {
            categoryData = new CategoryDataBuilder();
            categoryData.setCategoryKey(categoryKey.name());
            categoryBuilderMap.put(categoryKey, categoryData);
        }
        return categoryBuilderMap.get(categoryKey);

    }

    // get the category data object to be able to add items.
    private void addCategoryData(final NotificationEntity notification, final CategoryDataBuilder categoryData) {
        final Map<String, Object> dataSet = new HashMap<>();
        final NotificationCategoryEnum categoryKey = getCategoryKey(notification);

        int count = 1;
        if (categoryKey == NotificationCategoryEnum.HIGH_VULNERABILITY || categoryKey == NotificationCategoryEnum.MEDIUM_VULNERABILITY || categoryKey == NotificationCategoryEnum.LOW_VULNERABILITY) {
            count = notification.getVulnerabilityList().size();
            dataSet.put(VulnerabilityCache.VULNERABILITY_ID_SET, getVulnerabilityIdSet(notification));
        }

        if (StringUtils.isNotBlank(notification.getPolicyRuleName())) {
            dataSet.put(ItemTypeEnum.RULE.name(), notification.getPolicyRuleName());
        }

        dataSet.put(ItemTypeEnum.COMPONENT.name(), notification.getComponentName());
        dataSet.put(ItemTypeEnum.VERSION.name(), notification.getComponentVersion());
        dataSet.put(ItemTypeEnum.COUNT.name(), count);

        categoryData.addItem(new ItemData(dataSet));
    }

    private NotificationCategoryEnum getCategoryKey(final NotificationEntity notification) {
        return NotificationCategoryEnum.valueOf(notification.getNotificationType());
    }

    private Set<String> getVulnerabilityIdSet(final NotificationEntity notification) {
        final Collection<VulnerabilityEntity> vulnerabilityList = notification.getVulnerabilityList();
        final Set<String> idSet = new HashSet<>();
        if (vulnerabilityList != null && !vulnerabilityList.isEmpty()) {
            vulnerabilityList.forEach(vulnerability -> {
                idSet.add(vulnerability.getVulnerabilityId());
            });
        }
        return idSet;
    }

    private Collection<ProjectData> collectProjectDataFromMap(final Map<String, ProjectDataBuilder> projectDataMap) {
        final Collection<ProjectData> dataList = new LinkedList<>();
        for (final ProjectDataBuilder builder : projectDataMap.values()) {
            dataList.add(builder.build());
        }
        return dataList;
    }

}
