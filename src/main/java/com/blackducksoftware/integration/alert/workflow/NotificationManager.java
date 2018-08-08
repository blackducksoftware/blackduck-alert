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
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.database.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.audit.AuditNotificationRepository;
import com.blackducksoftware.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.alert.database.entity.repository.NotificationContentRepository;
import com.blackducksoftware.integration.alert.database.entity.repository.VulnerabilityRepository;

@Component
@Transactional
public class NotificationManager {
    private final NotificationContentRepository notificationContentRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;

    @Autowired
    public NotificationManager(final NotificationContentRepository notificationContentRepository, final VulnerabilityRepository vulnerabilityRepository, final AuditEntryRepository auditEntryRepository,
            final AuditNotificationRepository auditNotificationRepository) {
        this.notificationContentRepository = notificationContentRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
    }

    public NotificationContent saveNotification(final NotificationContent notification) {
        return notificationContentRepository.save(notification);
    }

    public List<NotificationContent> findByIds(final List<Long> notificationIds) {
        return notificationContentRepository.findAllById(notificationIds);
    }

    public List<NotificationContent> findByCreatedAtBetween(final Date startDate, final Date endDate) {
        return notificationContentRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public List<NotificationContent> findByCreatedAtBefore(final Date date) {
        return notificationContentRepository.findByCreatedAtBefore(date);
    }

    public List<NotificationContent> findByCreatedAtBeforeDayOffset(final int dayOffset) {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.minusDays(dayOffset);
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        final Date date = Date.from(zonedDate.toInstant());
        return findByCreatedAtBefore(date);
    }

    public void deleteNotificationList(final List<NotificationContent> notifications) {
        notifications.forEach(this::deleteNotification);
    }

    public void deleteNotification(final NotificationContent notification) {
        notificationContentRepository.delete(notification);
        deleteAuditEntries(notification.getId());
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
