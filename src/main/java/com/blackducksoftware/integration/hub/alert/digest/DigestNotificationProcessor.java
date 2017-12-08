/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.digest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.digest.filter.NotificationEventManager;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryDataBuilder;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataBuilder;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.processor.VulnerabilityCache;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

@Component
public class DigestNotificationProcessor {
    private final NotificationEventManager eventManager;

    @Autowired
    public DigestNotificationProcessor(final NotificationEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public List<AbstractChannelEvent> processNotifications(final DigestTypeEnum digestType, final List<NotificationEntity> notificationList) {
        final DigestRemovalProcessor removalProcessor = new DigestRemovalProcessor();
        final List<NotificationEntity> processedNotificationList = removalProcessor.process(notificationList);
        if (processedNotificationList.isEmpty()) {
            return Collections.emptyList();
        } else {
            final Collection<ProjectData> userData = createCateoryDataMap(digestType, processedNotificationList);
            return eventManager.createChannelEvents(userData);
        }
    }

    private Collection<ProjectData> createCateoryDataMap(final DigestTypeEnum digestType, final Collection<NotificationEntity> eventMap) {
        final Map<String, ProjectDataBuilder> projectDataMap = new LinkedHashMap<>();
        for (final NotificationEntity entity : eventMap) {
            final String projectKey = entity.getEventKey();
            // get category map from the project or create the project data if it doesn't exist
            Map<NotificationCategoryEnum, CategoryDataBuilder> categoryBuilderMap;
            if (!projectDataMap.containsKey(projectKey)) {
                final ProjectDataBuilder projectBuilder = new ProjectDataBuilder();
                projectBuilder.setDigestType(digestType);
                projectBuilder.setProjectName(entity.getProjectName());
                projectBuilder.setProjectVersion(entity.getProjectVersion());
                projectDataMap.put(projectKey, projectBuilder);
                categoryBuilderMap = projectBuilder.getCategoryBuilderMap();
            } else {
                final ProjectDataBuilder projectBuilder = projectDataMap.get(projectKey);
                categoryBuilderMap = projectBuilder.getCategoryBuilderMap();
            }
            // get the category data object to be able to add items.
            CategoryDataBuilder categoryData;
            final NotificationCategoryEnum categoryKey = NotificationCategoryEnum.valueOf(entity.getNotificationType());
            if (!categoryBuilderMap.containsKey(categoryKey)) {
                categoryData = new CategoryDataBuilder();
                categoryData.setCategoryKey(categoryKey.name());
                categoryBuilderMap.put(categoryKey, categoryData);
            } else {
                categoryData = categoryBuilderMap.get(categoryKey);
            }
            int count = 1;
            final Map<String, Object> dataSet = new HashMap<>();
            if (categoryKey == NotificationCategoryEnum.HIGH_VULNERABILITY || categoryKey == NotificationCategoryEnum.MEDIUM_VULNERABILITY || categoryKey == NotificationCategoryEnum.LOW_VULNERABILITY) {
                count = entity.getVulnerabilityList().size();
                dataSet.put(VulnerabilityCache.VULNERABILITY_ID_SET, getVulnerabilityIdSet(entity));
            }

            if (StringUtils.isNotBlank(entity.getPolicyRuleName())) {
                dataSet.put(ItemTypeEnum.RULE.name(), entity.getPolicyRuleName());
            }

            dataSet.put(ItemTypeEnum.COMPONENT.name(), entity.getComponentName());
            dataSet.put(ItemTypeEnum.VERSION.name(), entity.getComponentVersion());
            dataSet.put(ItemTypeEnum.COUNT.name(), count);

            categoryData.addItem(new ItemData(dataSet));
        }
        // build
        final Collection<ProjectData> dataList = new LinkedList<>();
        for (final ProjectDataBuilder builder : projectDataMap.values()) {
            dataList.add(builder.build());
        }
        return dataList;
    }

    private Set<String> getVulnerabilityIdSet(final NotificationEntity entity) {
        final Collection<VulnerabilityEntity> vulnerabilityList = entity.getVulnerabilityList();
        final Set<String> idSet = new HashSet<>();
        if (vulnerabilityList != null && !vulnerabilityList.isEmpty()) {
            vulnerabilityList.forEach(vulnerability -> {
                idSet.add(vulnerability.getVulnerabilityId());
            });
        }

        return idSet;
    }

}
