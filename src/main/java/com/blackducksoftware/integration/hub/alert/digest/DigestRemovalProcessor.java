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
package com.blackducksoftware.integration.hub.alert.digest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.VulnerabilityOperationEnum;
import com.blackducksoftware.integration.hub.alert.model.NotificationModel;

public class DigestRemovalProcessor {
    private final Map<String, Map<NotificationCategoryEnum, NotificationModel>> entityCache;
    private final Map<String, Map<NotificationCategoryEnum, Set<String>>> vulnerabilityCache;

    public DigestRemovalProcessor() {
        entityCache = new HashMap<>();
        vulnerabilityCache = new HashMap<>();
    }

    public List<NotificationModel> process(final List<NotificationModel> notificationList) {
        final List<NotificationModel> resultList = new ArrayList<>();

        notificationList.stream().forEachOrdered(entity -> {
            final Map<NotificationCategoryEnum, NotificationModel> categoryMap;
            final String cacheKey = createCacheKey(entity);
            if (entityCache.containsKey(cacheKey)) {
                categoryMap = entityCache.get(cacheKey);
            } else {
                categoryMap = new HashMap<>();
                entityCache.put(cacheKey, categoryMap);
            }

            final boolean processed = processPolicyNotifications(categoryMap, entity);
            if (!processed) {
                processVulnerabilityNotifications(cacheKey, categoryMap, entity);
            }
        });

        entityCache.values().forEach(categoryMap -> {
            resultList.addAll(categoryMap.values());
        });
        return resultList;
    }

    private String createCacheKey(final NotificationModel entity) {
        return entity.getEventKey();
    }

    private boolean processPolicyNotifications(final Map<NotificationCategoryEnum, NotificationModel> categoryMap, final NotificationModel entity) {
        final NotificationCategoryEnum notificationType = entity.getNotificationType();
        if (NotificationCategoryEnum.POLICY_VIOLATION.equals(notificationType)) {
            categoryMap.put(notificationType, entity);
            return true;
        } else if (NotificationCategoryEnum.POLICY_VIOLATION_CLEARED.equals(notificationType) || NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE.equals(notificationType)) {
            if (categoryMap.containsKey(notificationType)) {
                categoryMap.remove(notificationType);
            } else if (categoryMap.containsKey(NotificationCategoryEnum.POLICY_VIOLATION)) {
                categoryMap.remove(NotificationCategoryEnum.POLICY_VIOLATION);
            } else {
                categoryMap.put(notificationType, entity);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean processVulnerabilityNotifications(final String cacheKey, final Map<NotificationCategoryEnum, NotificationModel> categoryMap, final NotificationModel entity) {
        final NotificationCategoryEnum notificationType = entity.getNotificationType();
        final Collection<VulnerabilityEntity> vulnerabilities = entity.getVulnerabilityList();
        final Map<NotificationCategoryEnum, Set<String>> vulnerabilityCategoryMap = vulnerabilityCache.containsKey(cacheKey) ? vulnerabilityCache.get(cacheKey) : new HashMap<>();
        final Set<String> vulnerabilityIds;
        if (vulnerabilityCategoryMap.containsKey(notificationType)) {
            vulnerabilityIds = vulnerabilityCategoryMap.get(notificationType);
        } else {
            vulnerabilityIds = new HashSet<>();
            vulnerabilityCategoryMap.put(notificationType, vulnerabilityIds);
        }
        if (!vulnerabilities.isEmpty()) {
            vulnerabilities.forEach(vulnerabilityEntity -> {
                final VulnerabilityOperationEnum operation = vulnerabilityEntity.getOperation();
                final String id = vulnerabilityEntity.getVulnerabilityId();
                if (VulnerabilityOperationEnum.DELETE.equals(operation)) {
                    if (vulnerabilityIds.contains(id)) {
                        vulnerabilityIds.remove(id);
                    } else {
                        vulnerabilityIds.add(id);
                    }
                } else {
                    vulnerabilityIds.add(id);
                }
            });
        }
        if (vulnerabilityIds.isEmpty()) {
            vulnerabilityCategoryMap.remove(notificationType);
            categoryMap.remove(notificationType);
            if (vulnerabilityCategoryMap.isEmpty()) {
                vulnerabilityCache.remove(cacheKey);
                entityCache.remove(cacheKey);
            }
        } else {
            vulnerabilityCache.put(cacheKey, vulnerabilityCategoryMap);
            categoryMap.put(notificationType, entity);
        }
        return false;
    }
}
