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

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.enumeration.VulnerabilityOperationEnum;
import com.blackducksoftware.integration.hub.alert.model.NotificationModel;
import com.blackducksoftware.integration.hub.throwaway.ItemTypeEnum;

@Component
public class ProjectDataFactory {
    public final static String VULNERABILITY_COUNT_KEY_DELETED = "DELETED";
    public final static String VULNERABILITY_COUNT_KEY_UPDATED = "UPDATED";
    public final static String VULNERABILITY_COUNT_KEY_ADDED = "ADDED";
    public final static String VULNERABILITY_ADDED_ID_SET = "vulnerabilityAddedIdSet";
    public final static String VULNERABILITY_UPDATED_ID_SET = "vulnerabilityUpdatedIdSet";
    public final static String VULNERABILITY_DELETED_ID_SET = "vulnerabilityDeletedIdSet";

    public Collection<ProjectData> createProjectDataCollection(final Collection<NotificationModel> notifications) {
        return createProjectDataCollection(notifications, DigestTypeEnum.REAL_TIME);
    }

    public Collection<ProjectData> createProjectDataCollection(final Collection<NotificationModel> notifications, final DigestTypeEnum digestType) {
        final Map<String, ProjectDataBuilder> projectDataMap = new LinkedHashMap<>();
        for (final NotificationModel entity : notifications) {
            final String projectKey = entity.getProjectName() + entity.getProjectVersion();

            final ProjectDataBuilder projectDataBuilder;
            if (projectDataMap.containsKey(projectKey)) {
                projectDataBuilder = projectDataMap.get(projectKey);
            } else {
                projectDataBuilder = getProjectDataBuilder(entity, digestType);
                projectDataMap.put(projectKey, projectDataBuilder);
            }
            projectDataBuilder.addNotificationId(entity.getNotificationEntity().getId());
            final CategoryDataBuilder categoryDataBuilder = getCategoryDataBuilder(entity, projectDataBuilder.getCategoryBuilderMap());
            addCategoryData(entity, categoryDataBuilder);
        }
        return collectProjectDataFromMap(projectDataMap);
    }

    public ProjectData createProjectData(final NotificationModel notification) {
        return createProjectData(notification, DigestTypeEnum.REAL_TIME);
    }

    public ProjectData createProjectData(final NotificationModel notification, final DigestTypeEnum digestType) {
        final ProjectDataBuilder projectDataBuilder = getProjectDataBuilder(notification, digestType);
        projectDataBuilder.addNotificationId(notification.getNotificationEntity().getId());
        final CategoryDataBuilder categoryData = getCategoryDataBuilder(notification, projectDataBuilder.getCategoryBuilderMap());
        categoryData.setCategoryKey(notification.getNotificationType().name());
        addCategoryData(notification, categoryData);
        return projectDataBuilder.build();
    }

    // get category map from the project or create the project data if it doesn't exist
    private ProjectDataBuilder getProjectDataBuilder(final NotificationModel notification, final DigestTypeEnum digestType) {
        final ProjectDataBuilder projectBuilder;
        projectBuilder = new ProjectDataBuilder();
        projectBuilder.setDigestType(digestType);
        projectBuilder.setProjectName(notification.getProjectName());
        projectBuilder.setProjectVersion(notification.getProjectVersion());
        return projectBuilder;
    }

    private CategoryDataBuilder getCategoryDataBuilder(final NotificationModel notification, final Map<NotificationCategoryEnum, CategoryDataBuilder> categoryBuilderMap) {
        final CategoryDataBuilder categoryData;
        final NotificationCategoryEnum categoryKey = notification.getNotificationType();
        if (!categoryBuilderMap.containsKey(categoryKey)) {
            categoryData = new CategoryDataBuilder();
            categoryData.setCategoryKey(categoryKey.name());
            categoryBuilderMap.put(categoryKey, categoryData);
        }
        return categoryBuilderMap.get(categoryKey);

    }

    // get the category data object to be able to add items.
    private void addCategoryData(final NotificationModel notification, final CategoryDataBuilder categoryData) {
        final Map<String, Object> dataSet = new HashMap<>();
        final NotificationCategoryEnum categoryKey = notification.getNotificationType();

        int countAdded = 0;
        int countUpdated = 0;
        int countDeleted = 0;
        if (categoryKey == NotificationCategoryEnum.HIGH_VULNERABILITY || categoryKey == NotificationCategoryEnum.MEDIUM_VULNERABILITY || categoryKey == NotificationCategoryEnum.LOW_VULNERABILITY) {
            addVulnerabilitySets(dataSet, notification);
            countAdded = dataSet.containsKey(VULNERABILITY_ADDED_ID_SET) ? ((Set<?>) dataSet.get(VULNERABILITY_ADDED_ID_SET)).size() : 0;
            countUpdated = dataSet.containsKey(VULNERABILITY_UPDATED_ID_SET) ? ((Set<?>) dataSet.get(VULNERABILITY_UPDATED_ID_SET)).size() : 0;
            countDeleted = dataSet.containsKey(VULNERABILITY_DELETED_ID_SET) ? ((Set<?>) dataSet.get(VULNERABILITY_DELETED_ID_SET)).size() : 0;
        }

        if (StringUtils.isNotBlank(notification.getPolicyRuleName())) {
            dataSet.put(ItemTypeEnum.RULE.name(), notification.getPolicyRuleName());
        }

        dataSet.put(ItemTypeEnum.COMPONENT.name(), notification.getComponentName());
        dataSet.put(ItemTypeEnum.VERSION.name(), notification.getComponentVersion());
        if (countAdded > 0) {
            dataSet.put(VULNERABILITY_COUNT_KEY_ADDED, countAdded);
        }
        if (countUpdated > 0) {
            dataSet.put(VULNERABILITY_COUNT_KEY_UPDATED, countUpdated);
        }
        if (countDeleted > 0) {
            dataSet.put(VULNERABILITY_COUNT_KEY_DELETED, countDeleted);
        }

        categoryData.addItem(new ItemData(dataSet));
    }

    private void addVulnerabilitySets(final Map<String, Object> dataSet, final NotificationModel notification) {
        final Collection<VulnerabilityEntity> vulnerabilityList = notification.getVulnerabilityList();
        final Set<String> addedIdSet = new HashSet<>();
        final Set<String> updatedIdSet = new HashSet<>();
        final Set<String> deletedIdSet = new HashSet<>();
        if (vulnerabilityList != null && !vulnerabilityList.isEmpty()) {
            vulnerabilityList.forEach(vulnerability -> {
                if (vulnerability.getOperation() == VulnerabilityOperationEnum.ADD) {
                    addedIdSet.add(vulnerability.getVulnerabilityId());
                } else if (vulnerability.getOperation() == VulnerabilityOperationEnum.UPDATE) {
                    updatedIdSet.add(vulnerability.getVulnerabilityId());
                } else {
                    deletedIdSet.add(vulnerability.getVulnerabilityId());
                }
            });
        }
        if (!addedIdSet.isEmpty()) {
            dataSet.put(VULNERABILITY_ADDED_ID_SET, addedIdSet);
        }
        if (!updatedIdSet.isEmpty()) {
            dataSet.put(VULNERABILITY_UPDATED_ID_SET, updatedIdSet);
        }
        if (!deletedIdSet.isEmpty()) {
            dataSet.put(VULNERABILITY_DELETED_ID_SET, deletedIdSet);
        }
    }

    private Collection<ProjectData> collectProjectDataFromMap(final Map<String, ProjectDataBuilder> projectDataMap) {
        final Collection<ProjectData> dataList = new LinkedList<>();
        for (final ProjectDataBuilder builder : projectDataMap.values()) {
            dataList.add(builder.build());
        }
        return dataList;
    }

}
