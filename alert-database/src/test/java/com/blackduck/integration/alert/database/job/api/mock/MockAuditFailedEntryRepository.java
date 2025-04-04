/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api.mock;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.blackduck.integration.alert.database.audit.AuditFailedEntity;
import com.blackduck.integration.alert.database.audit.AuditFailedEntryRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAuditFailedEntryRepository extends MockRepositoryContainer<UUID, AuditFailedEntity> implements AuditFailedEntryRepository {
    public MockAuditFailedEntryRepository(Function<AuditFailedEntity, UUID> idGenerator) {
        super(idGenerator);
    }

    @Override
    public Page<AuditFailedEntity> findAllWithSearchTerm(String searchTerm, Pageable pageable) {
        return Page.empty();
    }

    @Override
    public List<AuditFailedEntity> findAllByCreatedAtBefore(OffsetDateTime expirationDate) {
        Predicate<AuditFailedEntity> dateAfterExpiration = entry -> entry.getCreatedAt().isBefore(expirationDate);
        return getDataMap().values().stream()
            .filter(dateAfterExpiration)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNotificationId(Long notificationId) {
        Predicate<AuditFailedEntity> entryContainsNotification = entry -> entry.getNotificationId().equals(notificationId);
        return getDataMap().values()
            .stream()
            .anyMatch(entryContainsNotification);
    }

    @Override
    public boolean existsByJobNameAndNotificationId(String jobName, Long notificationId) {
        Predicate<AuditFailedEntity> entryContainsJobName = entry -> entry.getJobName().equals(jobName);
        Predicate<AuditFailedEntity> entryContainsNotification = entry -> entry.getNotificationId().equals(notificationId);
        return getDataMap().values()
            .stream()
            .anyMatch(entryContainsJobName.and(entryContainsNotification));
    }

    @Override
    public void deleteAllByNotificationId(Long notificationId) {
        Predicate<AuditFailedEntity> entryContainsNotification = entry -> entry.getNotificationId().equals(notificationId);
        List<AuditFailedEntity> entitiesToDelete = getDataMap().values()
            .stream()
            .filter(entryContainsNotification)
            .collect(Collectors.toList());
        deleteAll(entitiesToDelete);
    }

    @Override
    public void deleteAllByJobNameAndNotificationId(String jobName, Long notificationId) {
        Predicate<AuditFailedEntity> entryContainsJobName = entry -> entry.getJobName().equals(jobName);
        Predicate<AuditFailedEntity> entryContainsNotification = entry -> entry.getNotificationId().equals(notificationId);
        List<AuditFailedEntity> entitiesToDelete = getDataMap().values()
            .stream()
            .filter(entryContainsJobName.and(entryContainsNotification))
            .collect(Collectors.toList());
        deleteAll(entitiesToDelete);
    }
}
