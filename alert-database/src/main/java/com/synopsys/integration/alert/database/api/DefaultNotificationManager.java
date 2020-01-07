/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.database.api;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.NotificationEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;

@Component
@Transactional
public class DefaultNotificationManager implements NotificationManager {
    private final NotificationContentRepository notificationContentRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final EventManager eventManager;

    @Autowired
    public DefaultNotificationManager(final NotificationContentRepository notificationContentRepository, final AuditEntryRepository auditEntryRepository, final AuditNotificationRepository auditNotificationRepository,
        final EventManager eventManager) {
        this.notificationContentRepository = notificationContentRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.eventManager = eventManager;
    }

    @Override
    public List<AlertNotificationWrapper> saveAllNotifications(final Collection<AlertNotificationWrapper> notifications) {
        final List<AlertNotificationWrapper> notificationContents = notifications.stream()
                                                                        .map(notification -> notificationContentRepository.save((NotificationContent) notification))
                                                                        .collect(Collectors.toList());
        if (!notificationContents.isEmpty()) {
            final List<Long> notificationIds = notificationContents.stream()
                                                   .map(AlertNotificationWrapper::getId)
                                                   .collect(Collectors.toList());
            eventManager.sendEvent(new NotificationEvent(notificationIds));
        }
        return notificationContents;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<AlertNotificationWrapper> findAll(final PageRequest pageRequest, final boolean onlyShowSentNotifications) {
        if (onlyShowSentNotifications) {
            final Page<NotificationContent> allSentNotifications = notificationContentRepository.findAllSentNotifications(pageRequest);
            return safelyConvertToGenericPage(allSentNotifications);
        }
        return safelyConvertToGenericPage(notificationContentRepository.findAll(pageRequest));
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<AlertNotificationWrapper> findAllWithSearch(final String searchTerm, final PageRequest pageRequest, final boolean onlyShowSentNotifications) {
        final String lcSearchTerm = searchTerm.toLowerCase(Locale.ENGLISH);

        final Page<NotificationContent> matchingNotifications;
        if (onlyShowSentNotifications) {
            matchingNotifications = notificationContentRepository.findMatchingSentNotification(lcSearchTerm, pageRequest);
        } else {
            matchingNotifications = notificationContentRepository.findMatchingNotification(lcSearchTerm, pageRequest);
        }
        return safelyConvertToGenericPage(matchingNotifications);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationWrapper> findByIds(final List<Long> notificationIds) {
        return safelyConvertToGenericList(notificationContentRepository.findAllById(notificationIds));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<AlertNotificationWrapper> findById(final Long notificationId) {
        return safelyConvertToGenericOptional(notificationContentRepository.findById(notificationId));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationWrapper> findByCreatedAtBetween(final Date startDate, final Date endDate) {
        final List<NotificationContent> byCreatedAtBetween = notificationContentRepository.findByCreatedAtBetween(startDate, endDate);
        return safelyConvertToGenericList(byCreatedAtBetween);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationWrapper> findByCreatedAtBefore(final Date date) {
        final List<NotificationContent> byCreatedAtBefore = notificationContentRepository.findByCreatedAtBefore(date);
        return safelyConvertToGenericList(byCreatedAtBefore);
    }

    @Override
    public List<AlertNotificationWrapper> findByCreatedAtBeforeDayOffset(final int dayOffset) {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.minusDays(dayOffset);
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        final Date date = Date.from(zonedDate.toInstant());
        return findByCreatedAtBefore(date);
    }

    @Override
    public void deleteNotificationList(final List<AlertNotificationWrapper> notifications) {
        notifications.forEach(this::deleteNotification);
    }

    @Override
    public void deleteNotification(final AlertNotificationWrapper notification) {
        deleteAuditEntries(notification.getId());
        notificationContentRepository.deleteById(notification.getId());
    }

    public PageRequest getPageRequestForNotifications(final Integer pageNumber, final Integer pageSize, final String sortField, final String sortOrder) {
        final Integer page = ObjectUtils.defaultIfNull(pageNumber, 0);
        final Integer size = ObjectUtils.defaultIfNull(pageSize, Integer.MAX_VALUE);
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
        if (StringUtils.isNotBlank(sortOrder) && sortQuery && Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
            sortingOrder = Sort.Direction.ASC;
        }
        return PageRequest.of(page, size, new Sort(sortingOrder, sortingField));
    }

    private void deleteAuditEntries(final Long notificationId) {
        final List<AuditNotificationRelation> foundRelations = auditNotificationRepository.findByNotificationId(notificationId);
        final List<Long> auditIdList = foundRelations
                                           .stream()
                                           .map(AuditNotificationRelation::getAuditEntryId)
                                           .collect(Collectors.toList());
        final List<AuditEntryEntity> auditEntryList = auditEntryRepository.findAllById(auditIdList);
        auditEntryRepository.deleteAll(auditEntryList);
    }

    private Page<AlertNotificationWrapper> safelyConvertToGenericPage(final Page<NotificationContent> notificationPage) {
        return notificationPage.map(AlertNotificationWrapper.class::cast);
    }

    private Optional<AlertNotificationWrapper> safelyConvertToGenericOptional(final Optional<NotificationContent> notificationContent) {
        return notificationContent.map(AlertNotificationWrapper.class::cast);
    }

    private List<AlertNotificationWrapper> safelyConvertToGenericList(final List<NotificationContent> notificationContents) {
        final List<AlertNotificationWrapper> wrappers = new ArrayList<>(notificationContents.size());
        notificationContents.forEach(wrappers::add);
        return wrappers;
    }

}
