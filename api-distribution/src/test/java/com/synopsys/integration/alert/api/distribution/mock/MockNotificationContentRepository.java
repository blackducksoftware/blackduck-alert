package com.synopsys.integration.alert.api.distribution.mock;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockNotificationContentRepository extends MockRepositoryContainer<Long, NotificationEntity> implements NotificationContentRepository {
    public MockNotificationContentRepository(final Function<NotificationEntity, Long> idGenerator) {
        super(idGenerator);
    }

    @Override
    public Page<NotificationEntity> findByCreatedAtBetween(final OffsetDateTime startDate, final OffsetDateTime endDate, final Pageable pageable) {
        Predicate<NotificationEntity> beforePredicate = notification -> notification.getCreatedAt().isBefore(endDate) || notification.getCreatedAt().isEqual(endDate);
        Predicate<NotificationEntity> afterPredicate = notification -> notification.getCreatedAt().isAfter(startDate) || notification.getCreatedAt().isEqual(startDate);
        Predicate<NotificationEntity> withinRange = beforePredicate.and(afterPredicate);
        List<NotificationEntity> notifications = findAll().stream()
            .sorted(Comparator.comparing(NotificationEntity::getCreatedAt))
            .filter(withinRange)
            .collect(Collectors.toList());
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
            .collect(Collectors.toList());
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
            .collect(Collectors.toList());
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
            .collect(Collectors.toList());
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
            .collect(Collectors.toList());
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
            .collect(Collectors.toList());
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
}
