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
package com.blackducksoftware.integration.hub.alert.batch.digest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.hub.alert.batch.digest.model.CategoryDataBuilder;
import com.blackducksoftware.integration.hub.alert.batch.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.batch.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.batch.digest.model.ProjectDataBuilder;
import com.blackducksoftware.integration.hub.alert.batch.digest.processor.NotificationRemovalProcessor;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailEvent;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatEvent;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.processor.VulnerabilityCache;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class DigestItemProcessor implements ItemProcessor<List<NotificationEntity>, List<AbstractChannelEvent>> {
    private final static Logger logger = LoggerFactory.getLogger(DigestItemProcessor.class);

    @Override
    public List<AbstractChannelEvent> process(final List<NotificationEntity> notificationData) throws Exception {
        logger.info("Notification Entity Count: {}", notificationData.size());

        final List<AbstractChannelEvent> events = processNotifications(notificationData);

        if (events.isEmpty()) {
            return null;
        } else {
            return events;
        }
    }

    private List<AbstractChannelEvent> processNotifications(final List<NotificationEntity> notificationList) {
        final NotificationRemovalProcessor removalProcessor = new NotificationRemovalProcessor();
        final List<NotificationEntity> processedNotificationList = removalProcessor.process(notificationList);
        if (notificationList == null) {
            return new ArrayList<>(0);
        } else {
            final Collection<ProjectData> projectDataList = createCateoryDataMap(processedNotificationList);
            final List<AbstractChannelEvent> events = new ArrayList<>(projectDataList.size());
            projectDataList.forEach(projectData -> {
                events.add(new EmailEvent(projectData));
                events.add(new HipChatEvent(projectData));
            });
            return events;
        }
    }

    private Collection<ProjectData> createCateoryDataMap(final Collection<NotificationEntity> eventMap) {
        final Map<String, ProjectDataBuilder> projectDataMap = new LinkedHashMap<>();
        for (final NotificationEntity entry : eventMap) {
            final String projectKey = entry.getEventKey();
            // get category map from the project or create the project data if
            // it doesn't exist
            Map<NotificationCategoryEnum, CategoryDataBuilder> categoryBuilderMap;
            if (!projectDataMap.containsKey(projectKey)) {
                final ProjectDataBuilder projectBuilder = new ProjectDataBuilder();
                projectBuilder.setProjectName(entry.getProjectName());
                projectBuilder.setProjectVersion(entry.getProjectVersion());
                projectDataMap.put(projectKey, projectBuilder);
                categoryBuilderMap = projectBuilder.getCategoryBuilderMap();
            } else {
                final ProjectDataBuilder projectBuilder = projectDataMap.get(projectKey);
                categoryBuilderMap = projectBuilder.getCategoryBuilderMap();
            }
            // get the category data object to be able to add items.
            CategoryDataBuilder categoryData;
            final NotificationCategoryEnum categoryKey = NotificationCategoryEnum.valueOf(entry.getNotificationType());
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
                count = entry.getVulnerabilityList().size();
                dataSet.put(VulnerabilityCache.VULNERABILITY_ID_SET, getVulnerabilityIdSet(entry));
            }

            if (StringUtils.isNotBlank(entry.getPolicyRuleName())) {
                dataSet.put(ItemTypeEnum.RULE.name(), entry.getPolicyRuleName());
            }

            dataSet.put(ItemTypeEnum.COMPONENT.name(), entry.getComponentName());
            dataSet.put(ItemTypeEnum.VERSION.name(), entry.getComponentVersion());
            dataSet.put(ItemTypeEnum.COUNT.name(), count);

            categoryData.incrementItemCount(count);
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
