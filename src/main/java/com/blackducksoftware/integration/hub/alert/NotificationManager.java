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
package com.blackducksoftware.integration.hub.alert;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditNotificationRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.audit.repository.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.VulnerabilityRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;

@Component
public class NotificationManager {
    private final NotificationRepositoryWrapper notificationRepository;
    private final VulnerabilityRepositoryWrapper vulnerabilityRepository;
    private final AuditEntryRepositoryWrapper auditEntryRepository;
    private final AuditNotificationRepositoryWrapper auditNotificationRepositoryWrapper;

    @Autowired
    public NotificationManager(final NotificationRepositoryWrapper notificationRepository, final VulnerabilityRepositoryWrapper vulnerabilityRepository, final AuditEntryRepositoryWrapper auditEntryRepository,
            final AuditNotificationRepositoryWrapper auditNotificationRepositoryWrapper) {
        this.notificationRepository = notificationRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepositoryWrapper = auditNotificationRepositoryWrapper;
    }

    public NotificationModel saveNotification(final NotificationModel notification) {
        final NotificationEntity notificationEntity = notificationRepository.save(notification.getNotificationEntity());
        List<VulnerabilityEntity> vulnerabilities = Collections.emptyList();
        if (notification.getVulnerabilityList() != null) {
            final Collection<VulnerabilityEntity> vulnerabilityList = notification.getVulnerabilityList();
            final List<VulnerabilityEntity> vulnerabilitiesToSave = vulnerabilityList.stream()
                    .map(vulnerability -> new VulnerabilityEntity(vulnerability.getVulnerabilityId(), vulnerability.getOperation(), notificationEntity.getId()))
                    .collect(Collectors.toList());
            vulnerabilities = vulnerabilityRepository.save(vulnerabilitiesToSave);
        }

        return new NotificationModel(notificationEntity, vulnerabilities);
    }

    public List<NotificationModel> findByIds(final List<Long> notificationIds) {
        final List<NotificationEntity> notificationList = notificationRepository.findAll(notificationIds);
        return createModelList(notificationList);
    }

    public List<NotificationModel> findByCreatedAtBetween(final Date startDate, final Date endDate) {
        final List<NotificationEntity> notificationList = notificationRepository.findByCreatedAtBetween(startDate, endDate);
        return createModelList(notificationList);
    }

    public List<NotificationModel> findByCreatedAtBefore(final Date date) {
        final List<NotificationEntity> notificationList = notificationRepository.findByCreatedAtBefore(date);
        return createModelList(notificationList);
    }

    public List<NotificationModel> findByCreatedAtBeforeDayOffset(final int dayOffset) {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.minusDays(dayOffset);
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        final Date date = Date.from(zonedDate.toInstant());
        return findByCreatedAtBefore(date);
    }

    private List<NotificationModel> createModelList(final List<NotificationEntity> entityList) {
        final List<NotificationModel> resultList = new ArrayList<>();
        entityList.forEach(notification -> {
            final List<VulnerabilityEntity> vulnerabilities = vulnerabilityRepository.findByNotificationId(notification.getId());
            resultList.add(new NotificationModel(notification, vulnerabilities));
        });

        return resultList;
    }

    public void deleteNotificationList(final List<NotificationModel> notifications) {
        notifications.forEach(notification -> {
            deleteNotification(notification);
        });
    }

    public void deleteNotification(final NotificationModel model) {
        vulnerabilityRepository.delete(model.getVulnerabilityList());
        notificationRepository.delete(model.getNotificationEntity());
        deleteAuditEntries(model.getNotificationEntity().getId());
    }

    private void deleteAuditEntries(final Long notificationId) {
        final List<AuditNotificationRelation> foundRelations = auditNotificationRepositoryWrapper.findByNotificationId(notificationId);
        foundRelations.forEach(relation -> auditEntryRepository.delete(relation.getAuditEntryId()));
        auditNotificationRepositoryWrapper.delete(foundRelations);
    }

}
