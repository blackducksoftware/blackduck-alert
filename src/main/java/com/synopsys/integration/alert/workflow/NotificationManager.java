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
package com.synopsys.integration.alert.workflow;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.channel.JobConfigReader;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.NotificationContentRepository;
import com.synopsys.integration.alert.web.model.NotificationContentConverter;

@Component
@Transactional
public class NotificationManager {
    private final Logger logger = LoggerFactory.getLogger(NotificationManager.class);
    private final NotificationContentRepository notificationContentRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final NotificationContentConverter notificationContentConverter;
    private final JobConfigReader jobConfigReader;

    @Autowired
    public NotificationManager(final NotificationContentRepository notificationContentRepository, final AuditEntryRepository auditEntryRepository, final AuditNotificationRepository auditNotificationRepository,
        @Lazy final NotificationContentConverter notificationContentConverter, @Lazy final JobConfigReader jobConfigReader) {
        this.notificationContentRepository = notificationContentRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.notificationContentConverter = notificationContentConverter;
        this.jobConfigReader = jobConfigReader;
    }

    public NotificationContent saveNotification(final NotificationContent notification) {
        return notificationContentRepository.save(notification);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<NotificationContent> findAll(final PageRequest pageRequest, final boolean onlyShowSentNotifications) {
        if (onlyShowSentNotifications) {
            return notificationContentRepository.findAllSentNotifications(pageRequest);
        }
        return notificationContentRepository.findAll(pageRequest);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<NotificationContent> findAllWithSearch(final String searchTerm, final PageRequest pageRequest, final boolean onlyShowSentNotifications) {
        final String lcSearchTerm = searchTerm.toLowerCase(Locale.ENGLISH);
        if (onlyShowSentNotifications) {
            return notificationContentRepository.findMatchingSentNotification(lcSearchTerm, pageRequest);
        } else {
            return notificationContentRepository.findMatchingNotification(lcSearchTerm, pageRequest);
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<NotificationContent> findByIds(final List<Long> notificationIds) {
        return notificationContentRepository.findAllById(notificationIds);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<NotificationContent> findById(final Long notificationId) {
        return notificationContentRepository.findById(notificationId);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<NotificationContent> findByCreatedAtBetween(final Date startDate, final Date endDate) {
        return notificationContentRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
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

    public PageRequest getPageRequestForNotifications(final Integer pageNumber, final Integer pageSize, final String sortField, final String sortOrder) {
        boolean sortQuery = false;
        String sortingField = "createdAt";
        // We can only modify the query for the fields that exist in NotificationContent
        if (StringUtils.isNotBlank(sortField) && "createdAt".equalsIgnoreCase(sortField)
                || "provider".equalsIgnoreCase(sortField)
                || "providerCreationTime".equalsIgnoreCase(sortField)
                || "notificationType".equalsIgnoreCase(sortField)
                || "content".equalsIgnoreCase(sortField)) {
            sortingField = sortField;
            sortQuery = true;
        }
        Sort.Direction sortingOrder = Sort.Direction.DESC;
        if (StringUtils.isNotBlank(sortOrder) && sortQuery) {
            if (Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
                sortingOrder = Sort.Direction.ASC;
            }
        }
        return PageRequest.of(pageNumber, pageSize, new Sort(sortingOrder, sortingField));
    }

}
