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
package com.blackducksoftware.integration.hub.alert.batch.digest.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.processor.VulnerabilityOperation;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class NotificationRemovalProcessor {
    private Map<String, NotificationEntity> entityCache;
    private Map<String, Set<String>> vulnerabilityCache;

    public List<NotificationEntity> process(final List<NotificationEntity> notificationList) {
        final List<NotificationEntity> resultList = new ArrayList<>();

        notificationList.forEach(entity -> {
            final boolean processed = processPolicyNotifications(entity);
            if (!processed) {
                processVulnerabilityNotifications(entity);
            }
        });

        resultList.addAll(entityCache.values());
        return resultList;
    }

    private boolean processPolicyNotifications(final NotificationEntity entity) {
        final String notificationType = entity.getNotificationType();
        if (NotificationCategoryEnum.POLICY_VIOLATION.name().equals(notificationType)) {
            entityCache.put(entity.getEventKey(), entity);
            return true;
        } else if (NotificationCategoryEnum.POLICY_VIOLATION_CLEARED.name().equals(notificationType) || NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE.name().equals(notificationType)) {
            if (entityCache.containsKey(entity.getEventKey())) {
                entityCache.remove(entity.getEventKey());
            } else {
                entityCache.put(entity.getEventKey(), entity);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean processVulnerabilityNotifications(final NotificationEntity entity) {
        final String eventKey = entity.getEventKey();
        final Collection<VulnerabilityEntity> vulnerabilities = entity.getVulnerabilityList();
        final Set<String> vulnerabilityIds = vulnerabilityCache.containsKey(eventKey) ? vulnerabilityCache.get(eventKey) : new HashSet<>();

        if (!vulnerabilities.isEmpty()) {
            vulnerabilities.forEach(vulnerabilityEntity -> {
                final String operation = vulnerabilityEntity.getOperation();
                final String id = vulnerabilityEntity.getVulnerabilityId();
                if (VulnerabilityOperation.DELETE.name().equals(operation)) {
                    vulnerabilityIds.remove(id);
                } else {
                    vulnerabilityIds.add(id);
                }
            });
        }

        if (vulnerabilityIds.isEmpty()) {
            vulnerabilityCache.remove(eventKey);
            entityCache.remove(eventKey);
        } else {
            vulnerabilityCache.put(eventKey, vulnerabilityIds);
            if (!entityCache.containsKey(eventKey)) {
                entityCache.put(eventKey, entity);
            }
        }
        return false;
    }
}
