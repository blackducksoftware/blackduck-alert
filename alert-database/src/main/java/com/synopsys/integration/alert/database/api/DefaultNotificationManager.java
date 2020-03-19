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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
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
    private static final Logger logger = LoggerFactory.getLogger(DefaultNotificationManager.class);
    private final NotificationContentRepository notificationContentRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final EventManager eventManager;

    @Autowired
    public DefaultNotificationManager(NotificationContentRepository notificationContentRepository, AuditEntryRepository auditEntryRepository, AuditNotificationRepository auditNotificationRepository,
        EventManager eventManager) {
        this.notificationContentRepository = notificationContentRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.eventManager = eventManager;
    }

    @Override
    public List<AlertNotificationWrapper> saveAllNotifications(Collection<AlertNotificationWrapper> notifications) {
        List<AlertNotificationWrapper> notificationContents = notifications.stream()
                                                                  .map(notification -> notificationContentRepository.save((NotificationContent) notification))
                                                                  .collect(Collectors.toList());
        if (!notificationContents.isEmpty()) {
            List<Long> notificationIds = notificationContents.stream()
                                             .map(AlertNotificationWrapper::getId)
                                             .collect(Collectors.toList());
            eventManager.sendEvent(new NotificationEvent(notificationIds));
        }
        return notificationContents;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<AlertNotificationWrapper> findAll(PageRequest pageRequest, boolean onlyShowSentNotifications) {
        if (onlyShowSentNotifications) {
            Page<NotificationContent> allSentNotifications = notificationContentRepository.findAllSentNotifications(pageRequest);
            return safelyConvertToGenericPage(allSentNotifications);
        }
        return safelyConvertToGenericPage(notificationContentRepository.findAll(pageRequest));
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<AlertNotificationWrapper> findAllWithSearch(String searchTerm, PageRequest pageRequest, boolean onlyShowSentNotifications) {
        String lcSearchTerm = searchTerm.toLowerCase(Locale.ENGLISH);

        Page<NotificationContent> matchingNotifications;
        if (onlyShowSentNotifications) {
            matchingNotifications = notificationContentRepository.findMatchingSentNotification(lcSearchTerm, pageRequest);
        } else {
            matchingNotifications = notificationContentRepository.findMatchingNotification(lcSearchTerm, pageRequest);
        }
        return safelyConvertToGenericPage(matchingNotifications);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationWrapper> findByIds(List<Long> notificationIds) {
        return safelyConvertToGenericList(notificationContentRepository.findAllById(notificationIds));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<AlertNotificationWrapper> findById(Long notificationId) {
        return safelyConvertToGenericOptional(notificationContentRepository.findById(notificationId));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationWrapper> findByCreatedAtBetween(Date startDate, Date endDate) {
        List<NotificationContent> byCreatedAtBetween = notificationContentRepository.findByCreatedAtBetween(startDate, endDate);
        return safelyConvertToGenericList(byCreatedAtBetween);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationWrapper> findByCreatedAtBefore(Date date) {
        List<NotificationContent> byCreatedAtBefore = notificationContentRepository.findByCreatedAtBefore(date);
        return safelyConvertToGenericList(byCreatedAtBefore);
    }

    @Override
    public List<AlertNotificationWrapper> findByCreatedAtBeforeDayOffset(int dayOffset) {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.minusDays(dayOffset);
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        Date date = Date.from(zonedDate.toInstant());
        return findByCreatedAtBefore(date);
    }

    @Override
    public void deleteNotificationList(List<AlertNotificationWrapper> notifications) {
        notifications.forEach(this::deleteNotification);
    }

    @Override
    public void deleteNotification(AlertNotificationWrapper notification) {
        deleteAuditEntries(notification.getId());
        notificationContentRepository.deleteById(notification.getId());
    }

    public PageRequest getPageRequestForNotifications(Integer pageNumber, Integer pageSize, String sortField, String sortOrder) {
        Integer page = ObjectUtils.defaultIfNull(pageNumber, 0);
        Integer size = ObjectUtils.defaultIfNull(pageSize, Integer.MAX_VALUE);
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

    private void deleteAuditEntries(Long notificationId) {
        List<AuditNotificationRelation> foundRelations;
        try {
            foundRelations = auditNotificationRepository.findByNotificationId(notificationId);
            Function<AuditNotificationRelation, Long> transform = AuditNotificationRelation::getAuditEntryId;
            List<Long> auditIdList = foundRelations.stream().map(transform).collect(Collectors.toList());
            auditNotificationRepository.deleteAll(foundRelations);
            List<AuditEntryEntity> auditEntryList = auditEntryRepository.findAllById(auditIdList);
            auditEntryRepository.deleteAll(auditEntryList);
        } catch (JpaObjectRetrievalFailureException ex) {
            logger.error("Error deleting audit entry and relations based on notificationId.", ex);
            try {
                auditNotificationRepository.deleteByNotificationId(notificationId);
            } catch (JpaObjectRetrievalFailureException relationEx) {
                logger.error("Error deleting audit relation based on notificationId. ", relationEx);
            }
        }
    }

    private Page<AlertNotificationWrapper> safelyConvertToGenericPage(Page<NotificationContent> notificationPage) {
        return notificationPage.map(AlertNotificationWrapper.class::cast);
    }

    private Optional<AlertNotificationWrapper> safelyConvertToGenericOptional(Optional<NotificationContent> notificationContent) {
        return notificationContent.map(AlertNotificationWrapper.class::cast);
    }

    private List<AlertNotificationWrapper> safelyConvertToGenericList(List<NotificationContent> notificationContents) {
        List<AlertNotificationWrapper> wrappers = new ArrayList<>(notificationContents.size());
        notificationContents.forEach(wrappers::add);
        return wrappers;
    }

}
