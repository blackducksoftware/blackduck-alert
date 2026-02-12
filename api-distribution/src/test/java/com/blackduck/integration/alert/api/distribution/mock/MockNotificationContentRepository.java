/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.mock;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.blackduck.integration.alert.database.notification.NotificationBatchEntity;
import com.blackduck.integration.alert.database.notification.NotificationContentRepository;
import com.blackduck.integration.alert.database.notification.NotificationCountsPerHour;
import com.blackduck.integration.alert.database.notification.NotificationEntity;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockNotificationContentRepository extends MockRepositoryContainer<Long, NotificationEntity> implements NotificationContentRepository {
    private final MockNotificationBatchRepository mockNotificationBatchRepository;
    public MockNotificationContentRepository(final Function<NotificationEntity, Long> idGenerator) {
        super(idGenerator);
        mockNotificationBatchRepository = new MockNotificationBatchRepository();
    }

    @Override
    public <S extends NotificationEntity> @NotNull S save(@NotNull final S entity) {
        S savedEntity = super.save(entity);
        mockNotificationBatchRepository.save(new NotificationBatchEntity(entity.getProviderConfigId(), UUID.randomUUID(), entity.getId()));
        return savedEntity;
    }

    @Override
    public @NotNull <S extends NotificationEntity> List<S> saveAll(final Iterable<S> entities) {
        List<S> savedEntities = super.saveAll(entities);
        List<NotificationBatchEntity> batchEntities = new LinkedList<>();
        UUID batchId = UUID.randomUUID();
        entities.forEach(entity -> {
            NotificationBatchEntity batchEntity = new NotificationBatchEntity(entity.getProviderConfigId(), batchId, entity.getId());
            batchEntities.add(batchEntity);
        });
        mockNotificationBatchRepository.saveAll(batchEntities);
        return savedEntities;
    }

    @Override
    public Page<NotificationEntity> findByCreatedAtBetween(final OffsetDateTime startDate, final OffsetDateTime endDate, final Pageable pageable) {
        Predicate<NotificationEntity> beforePredicate = notification -> notification.getCreatedAt().isBefore(endDate) || notification.getCreatedAt().isEqual(endDate);
        Predicate<NotificationEntity> afterPredicate = notification -> notification.getCreatedAt().isAfter(startDate) || notification.getCreatedAt().isEqual(startDate);
        Predicate<NotificationEntity> withinRange = beforePredicate.and(afterPredicate);
        List<NotificationEntity> notifications = findAll().stream()
            .sorted(Comparator.comparing(NotificationEntity::getCreatedAt))
            .filter(withinRange)
            .toList();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        List<List<NotificationEntity>> partitionedLists = ListUtils.partition(notifications, pageSize);
        int totalPages = partitionedLists.size();
        if (partitionedLists.size() >= pageNumber) {
            return new PageImpl<>(partitionedLists.get(pageNumber), pageable, totalPages);
        } else {
            return new PageImpl<>(List.of());
        }
    }

    @Override
    public List<NotificationEntity> findByCreatedAtBefore(final OffsetDateTime date) {
        return findAll().stream()
            .filter(entity -> entity.getCreatedAt().isBefore(date))
            .toList();
    }

    @Override
    public Page<NotificationEntity> findAllSentNotifications(final Pageable pageable) {
        return null;
    }

    @Override
    public Page<NotificationEntity> findMatchingNotification(final String searchTerm, final Pageable pageable) {
        return null;
    }

    @Override
    public Page<NotificationEntity> findMatchingSentNotification(final String searchTerm, final Pageable pageable) {
        return null;
    }

    @Override
    public Page<NotificationEntity> findByProcessedFalseOrderByProviderCreationTimeAsc(final Pageable pageable) {
        Predicate<NotificationEntity> processedFalse = Predicate.not(NotificationEntity::getProcessed);
        List<NotificationEntity> notifications = findAll().stream()
            .sorted(Comparator.comparing(NotificationEntity::getProviderCreationTime))
            .filter(processedFalse)
            .toList();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        List<List<NotificationEntity>> partitionedLists = ListUtils.partition(notifications, pageSize);
        int totalPages = partitionedLists.size();
        if (partitionedLists.size() >= pageNumber) {
            return new PageImpl<>(partitionedLists.get(pageNumber), pageable, totalPages);
        } else {
            return new PageImpl<>(List.of());
        }
    }

    @Override
    public Page<NotificationEntity> findByProviderConfigIdAndProcessedFalseOrderByProviderCreationTimeAsc(long providerConfigId, Pageable pageable) {
        Predicate<NotificationEntity> processedFalse = Predicate.not(NotificationEntity::getProcessed);
        Predicate<NotificationEntity> providerConfigIdEqual = notificationEntity -> notificationEntity.getProviderConfigId().equals(providerConfigId);
        List<NotificationEntity> notifications = findAll().stream()
            .sorted(Comparator.comparing(NotificationEntity::getProviderCreationTime))
            .filter(providerConfigIdEqual)
            .filter(processedFalse)
            .toList();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        List<List<NotificationEntity>> partitionedLists = ListUtils.partition(notifications, pageSize);
        int totalPages = partitionedLists.size();
        if (partitionedLists.size() >= pageNumber) {
            return new PageImpl<>(partitionedLists.get(pageNumber), pageable, totalPages);
        } else {
            return new PageImpl<>(List.of());
        }
    }

    @Override
    public List<NotificationEntity> findAllByIdInOrderByProviderCreationTimeAsc(final List<Long> notificationIds) {
        return findAllById(notificationIds).stream()
            .sorted(Comparator.comparing(NotificationEntity::getProviderCreationTime))
            .toList();
    }

    @Override
    public void setProcessedByIds(Set<Long> notificationIds) {
        findAllById(notificationIds)
            .forEach(NotificationEntity::setProcessedToTrue);
    }

    @Override
    public int bulkDeleteCreatedAtBefore(OffsetDateTime date) {
        List<Long> notificationsToDelete = findByCreatedAtBefore(date).stream()
            .map(NotificationEntity::getId)
            .toList();
        deleteAllById(notificationsToDelete);
        return notificationsToDelete.size();
    }

    @Override
    public boolean existsByProcessedFalse() {
        Predicate<NotificationEntity> notProcessed = Predicate.not(NotificationEntity::getProcessed);
        return findAll().stream()
            .anyMatch(notProcessed);
    }

    @Override
    public boolean existsByProviderConfigIdAndProcessedFalse(long providerConfigId) {
        Predicate<NotificationEntity> providerConfigIdEqual = notificationEntity -> notificationEntity.getProviderConfigId().equals(providerConfigId);
        Predicate<NotificationEntity> notProcessed = Predicate.not(NotificationEntity::getProcessed);
        Predicate<NotificationEntity> providerAndProcessedFalse = providerConfigIdEqual.and(notProcessed);
        return findAll()
            .stream()
            .anyMatch(providerAndProcessedFalse);
    }

    @Override
    public long countByProcessed(boolean processed) {
        return findAll().stream()
            .filter(entity -> entity.getProcessed() == processed)
            .count();
    }

    @Override
    public boolean existsByContentId(String contentId) {
        return findAll()
            .stream()
            .map(NotificationEntity::getContentId)
            .anyMatch(contentId::equals);
    }

    @Override
    public long countByProviderConfigIdAndNotificationType(long providerConfigId, String notificationType) {
        Predicate<NotificationEntity> providerEqual = model -> model.getProviderConfigId().equals(providerConfigId);
        Predicate<NotificationEntity> notificationTypeEqual = model -> model.getNotificationType().equals(notificationType);
        return findAll()
            .stream()
            .filter(providerEqual.and(notificationTypeEqual))
            .count();
    }

    @Override
    public Page<NotificationEntity> findNotMappedAndNotProcessedNotifications(long providerConfigId, UUID batchId,  Pageable pageable) {
        Set<Long> notificationsInBatch = mockNotificationBatchRepository.findAll().stream()
            .filter(entity -> entity.getBatchId().equals(batchId))
            .map(NotificationBatchEntity::getNotificationId)
            .collect(Collectors.toSet());
        Predicate<NotificationEntity> notificationInBatch = notification -> notificationsInBatch.contains(notification.getId());
        Predicate<NotificationEntity> mappingFalse = Predicate.not(NotificationEntity::isMappingToJobs);
        Predicate<NotificationEntity> notProcessed = Predicate.not(NotificationEntity::getProcessed);
        Predicate<NotificationEntity> providerConfigIdEqual = notificationEntity -> notificationEntity.getProviderConfigId().equals(providerConfigId);
        List<NotificationEntity> notifications = findAll().stream()
                .sorted(Comparator.comparing(NotificationEntity::getProviderCreationTime))
                .filter(providerConfigIdEqual)
                .filter(notificationInBatch)
                .filter(mappingFalse)
                .filter(notProcessed)
                .toList();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        List<List<NotificationEntity>> partitionedLists = ListUtils.partition(notifications, pageSize);
        int totalPages = partitionedLists.size();
        if (partitionedLists.size() >= pageNumber) {
            return new PageImpl<>(partitionedLists.get(pageNumber), pageable, totalPages);
        } else {
            return new PageImpl<>(List.of());
        }
    }

    @Override
    public boolean existsByProviderConfigIdAndMappingToJobsFalse(long providerConfigId, UUID batchId) {
        Predicate<NotificationEntity> providerConfigIdEqual = notificationEntity -> notificationEntity.getProviderConfigId().equals(providerConfigId);
        Predicate<NotificationEntity> notMapped = Predicate.not(NotificationEntity::isMappingToJobs);
        Predicate<NotificationEntity> providerAndMappingToJobsFalse = providerConfigIdEqual.and(notMapped);
        return findAll()
                .stream()
                .anyMatch(providerAndMappingToJobsFalse);
    }

    @Override
    public void setMappingToJobsByIds(Set<Long> notificationIds) {
        findAllById(notificationIds)
                .forEach(NotificationEntity::setMappingToJobsToTrue);
    }

    @Override
    public void setMappingToJobsFalseWhenProcessedFalse(long providerConfigId) {
        Predicate<NotificationEntity> providerConfigIdEqual = notificationEntity -> notificationEntity.getProviderConfigId().equals(providerConfigId);
        Predicate<NotificationEntity> notProcessed = Predicate.not(NotificationEntity::getProcessed);
        Predicate<NotificationEntity> notMapped = Predicate.not(NotificationEntity::isMappingToJobs);
        Predicate<NotificationEntity> providerMappingAndProcessedFalse = providerConfigIdEqual.and(notMapped).and(notProcessed);

        List<NotificationEntity> entities = findAll().stream()
                .filter(providerMappingAndProcessedFalse)
                .toList();

        List<NotificationEntity> updatedEntities = entities.stream()
                .map(this::setMappingAndProcessedToFalse)
                .toList();

        saveAll(updatedEntities);
    }

    private NotificationEntity setMappingAndProcessedToFalse(NotificationEntity entity) {
        return new NotificationEntity(entity.getId(),
                entity.getCreatedAt(),
                entity.getProvider(),
                entity.getProviderConfigId(),
                entity.getProviderCreationTime(),
                entity.getNotificationType(),
                entity.getContent(),
                false,
                entity.getContentId(),
                false);
    }

    @Override
    public List<NotificationCountsPerHour> findNotificationCountsPerHourByProviderConfigId(final long providerConfigId) {
        Map<OffsetDateTime, Long> notificationsPerHourMap = findAll().stream()
            .filter(entity -> entity.getProviderConfigId().equals(providerConfigId))
            .collect(Collectors.groupingBy(NotificationEntity::getCreatedAt, Collectors.counting()));

        return notificationsPerHourMap.entrySet().stream()
            .map(entry -> new NotificationCountsPerHour(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(NotificationCountsPerHour::getAccumulationHour))
            .toList();
    }
}
