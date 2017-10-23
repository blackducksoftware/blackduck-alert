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
package com.blackducksoftware.integration.hub.alert.batch.accumulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.batch.item.ItemWriter;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.processor.PolicyViolationProcessor;
import com.blackducksoftware.integration.hub.alert.processor.VulnerabilityCache;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.report.api.PolicyRule;

public class AccumulatorWriter implements ItemWriter<DBStoreEvent> {
    private final NotificationRepository notificationRepository;

    public AccumulatorWriter(final NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void write(final List<? extends DBStoreEvent> itemList) throws Exception {
        itemList.forEach(item -> {
            final List<NotificationEvent> notificationList = item.getNotificationList();
            notificationList.forEach(notification -> {
                final String eventKey = notification.getEventKey();
                final NotificationContentItem content = (NotificationContentItem) notification.getDataSet().get(NotificationEvent.DATA_SET_KEY_NOTIFICATION_CONTENT);
                final Date createdAt = content.getCreatedAt();
                final String notificationType = notification.getCategoryType().toString();
                final String projectName = content.getProjectVersion().getProjectName();
                final String projectVersion = content.getProjectVersion().getProjectVersionName();
                final String componentName = content.getComponentName();
                final String componentVersion = content.getComponentVersion().versionName;
                final String policyRuleName = getPolicyRule(notification);
                final String person = getPerson(notification);
                final Collection<VulnerabilityEntity> vulnerabilityList = getVulnerabilities(notification);

                final NotificationEntity entity = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectVersion, componentName, componentVersion, policyRuleName, person, vulnerabilityList);
                notificationRepository.save(entity);
            });
        });
    }

    private String getPolicyRule(final NotificationEvent notification) {
        final String key = PolicyViolationProcessor.POLICY_RULE;
        if (notification.getDataSet().containsKey(key)) {
            final PolicyRule rule = (PolicyRule) notification.getDataSet().get(key);
            return rule.getName();
        } else {
            return "";
        }
    }

    private String getPerson(final NotificationEvent notification) {
        final String key = ItemTypeEnum.PERSON.name();
        if (notification.getDataSet().containsKey(key)) {
            final String person = (String) notification.getDataSet().get(key);
            return person;
        } else {
            return "";
        }
    }

    private Collection<VulnerabilityEntity> getVulnerabilities(final NotificationEvent notification) {
        final List<VulnerabilityEntity> vulnerabilityList = new ArrayList<>();
        final String key = VulnerabilityCache.VULNERABILITY_OPERATION;
        if (notification.getDataSet().containsKey(key)) {
            final String operationName = (String) notification.getDataSet().get(key);
            final Set<String> vulnerabilitySet = (Set<String>) notification.getDataSet().get(VulnerabilityCache.VULNERABILITY_ID_SET);

            if (!vulnerabilitySet.isEmpty()) {
                vulnerabilitySet.forEach(vulnerability -> {
                    final VulnerabilityEntity vulnerabilityEntity = new VulnerabilityEntity(vulnerability, operationName);
                    vulnerabilityList.add(vulnerabilityEntity);
                });
            }
        }

        return vulnerabilityList;
    }
}
