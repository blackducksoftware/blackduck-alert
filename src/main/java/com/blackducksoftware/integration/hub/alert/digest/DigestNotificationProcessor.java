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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.digest.filter.EventManager;
import com.blackducksoftware.integration.hub.alert.digest.filter.UserNotificationWrapper;
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
    private final static Logger logger = LoggerFactory.getLogger(DigestNotificationProcessor.class);
    private final EventManager eventManager;

    @Autowired
    public DigestNotificationProcessor(final EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public List<AbstractChannelEvent> processNotifications(final DigestTypeEnum digestType, final List<NotificationEntity> notificationList) {
        final DigestRemovalProcessor removalProcessor = new DigestRemovalProcessor();
        final List<NotificationEntity> processedNotificationList = removalProcessor.process(notificationList);
        if (processedNotificationList.isEmpty()) {
            return Collections.emptyList();
        } else {
            final Collection<UserNotificationWrapper> userData = createUserNotifications(digestType, processedNotificationList);
            return eventManager.createChannelEvents(userData);
        }
    }

    // TODO change map of maps to be its own object (Map<String, Map<String, ProjectDataBuilder>>)
    private Collection<UserNotificationWrapper> createUserNotifications(final DigestTypeEnum digestType, final Collection<NotificationEntity> notificationList) {
        final Map<String, Map<String, ProjectDataBuilder>> userProjectMap = createUserProjectMap(digestType, notificationList);
        final Collection<UserNotificationWrapper> dataList = new LinkedList<>();
        userProjectMap.entrySet().forEach(userMapEntry -> {
            final String username = userMapEntry.getKey();
            try {
                // FIXME
                // final HubUsersEntity userEntity = hubUsersRepository.findByUsername(username);
                // if (userEntity != null) {
                final Map<String, ProjectDataBuilder> projectDataMap = userMapEntry.getValue();
                final Set<ProjectData> projectDataSet = new LinkedHashSet<>();
                projectDataMap.values().forEach(projectDataBuilder -> {
                    projectDataSet.add(projectDataBuilder.build());
                });

                // dataList.add(new UserNotificationWrapper(userEntity.getId(), projectDataSet));
                // }
            } catch (final NoResultException ex) {
                logger.debug("user {} could not be found in the configuration", username);
                logger.debug("Cause:", ex);
            }
        });
        return dataList;
    }

    private Map<String, Map<String, ProjectDataBuilder>> createUserProjectMap(final DigestTypeEnum digestType, final Collection<NotificationEntity> notificationList) {
        final Map<String, Map<String, ProjectDataBuilder>> userProjectMap = new LinkedHashMap<>();
        notificationList.forEach(entry -> {
            final Map<NotificationCategoryEnum, CategoryDataBuilder> categoryBuilderMap = createCategoryBuilderMap(entry, digestType, userProjectMap);
            addCategoryData(entry, categoryBuilderMap);
        });
        return userProjectMap;
    }

    private Map<NotificationCategoryEnum, CategoryDataBuilder> createCategoryBuilderMap(final NotificationEntity entry, final DigestTypeEnum digestType, final Map<String, Map<String, ProjectDataBuilder>> userProjectMap) {
        final String userKey = entry.getHubUser();
        final String projectKey = entry.getEventKey();
        // get category map from the project or create the project data if it doesn't exist
        Map<NotificationCategoryEnum, CategoryDataBuilder> categoryBuilderMap;

        final Map<String, ProjectDataBuilder> projectDataMap;
        if (userProjectMap.containsKey(userKey)) {
            projectDataMap = userProjectMap.get(userKey);
        } else {
            projectDataMap = new LinkedHashMap<>();
            userProjectMap.put(userKey, projectDataMap);
        }

        if (!projectDataMap.containsKey(projectKey)) {
            final ProjectDataBuilder projectBuilder = new ProjectDataBuilder();
            projectBuilder.setDigestType(digestType);
            projectBuilder.setProjectName(entry.getProjectName());
            projectBuilder.setProjectVersion(entry.getProjectVersion());
            projectDataMap.put(projectKey, projectBuilder);
            categoryBuilderMap = projectBuilder.getCategoryBuilderMap();
        } else {
            final ProjectDataBuilder projectBuilder = projectDataMap.get(projectKey);
            categoryBuilderMap = projectBuilder.getCategoryBuilderMap();
        }
        return categoryBuilderMap;
    }

    private void addCategoryData(final NotificationEntity entry, final Map<NotificationCategoryEnum, CategoryDataBuilder> categoryBuilderMap) {
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

        categoryData.addItem(new ItemData(dataSet));
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
