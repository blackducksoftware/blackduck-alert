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
package com.blackducksoftware.integration.alert.workflow;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.audit.AuditNotificationRepository;
import com.blackducksoftware.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.alert.database.entity.NotificationEntity;
import com.blackducksoftware.integration.alert.database.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.NotificationRepository;
import com.blackducksoftware.integration.alert.database.entity.repository.VulnerabilityRepository;

@Component
public class NotificationManager {
    private final NotificationRepository notificationRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;

    @Autowired
    public NotificationManager(final NotificationRepository notificationRepository, final VulnerabilityRepository vulnerabilityRepository, final AuditEntryRepository auditEntryRepository,
            final AuditNotificationRepository auditNotificationRepository) {
        this.notificationRepository = notificationRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
    }

    public NotificationModel saveNotification(final NotificationModel notification) {
        final NotificationEntity notificationEntity = notificationRepository.save(notification.getNotificationEntity());
        List<VulnerabilityEntity> vulnerabilities = Collections.emptyList();
        if (notification.getVulnerabilityList() != null) {
            final Collection<VulnerabilityEntity> vulnerabilityList = notification.getVulnerabilityList();
            final List<VulnerabilityEntity> vulnerabilitiesToSave = vulnerabilityList.stream()
                    .map(vulnerability -> new VulnerabilityEntity(vulnerability.getVulnerabilityId(), vulnerability.getOperation(), notificationEntity.getId()))
                    .collect(Collectors.toList());
            vulnerabilities = vulnerabilityRepository.saveAll(vulnerabilitiesToSave);
        }

        return new NotificationModel(notificationEntity, vulnerabilities);
    }

    public List<NotificationModel> findByIds(final List<Long> notificationIds) {
        final List<NotificationEntity> notificationList = notificationRepository.findAllById(notificationIds);
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
        notifications.forEach(this::deleteNotification);
    }

    public void deleteNotification(final NotificationModel model) {
        vulnerabilityRepository.deleteAll(model.getVulnerabilityList());
        notificationRepository.delete(model.getNotificationEntity());
        deleteAuditEntries(model.getNotificationEntity().getId());
    }

    private void deleteAuditEntries(final Long notificationId) {
        final List<AuditNotificationRelation> foundRelations = auditNotificationRepository.findByNotificationId(notificationId);
        final Function<AuditNotificationRelation, Long> transform = AuditNotificationRelation::getAuditEntryId;
        final List<Long> auditIdList = foundRelations.stream().map(transform).collect(Collectors.toList());
        auditNotificationRepository.deleteAll(foundRelations);
        final List<AuditEntryEntity> auditEntryList = auditEntryRepository.findAllById(auditIdList);
        auditEntryRepository.deleteAll(auditEntryList);

    }

}
