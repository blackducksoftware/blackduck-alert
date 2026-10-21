/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.configuration.ConfigContextEntity;
import com.blackduck.integration.alert.database.configuration.DescriptorConfigEntity;
import com.blackduck.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.blackduck.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.blackduck.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.blackduck.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

@Transactional
@AlertIntegrationTest
@Tag(TestTags.DEFAULT_INTEGRATION)
class NotificationContentRepositoryTestIT {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private NotificationBatchRepository notificationBatchRepository;

    private DescriptorConfigEntity providerConfig;

    @BeforeEach
    void setUp() {
        providerConfig = createProviderConfig();
    }

    @AfterEach
    void cleanup() {
        notificationBatchRepository.deleteAll();
        notificationContentRepository.deleteAll();
        descriptorConfigRepository.delete(providerConfig);
    }

    @Test
    void findByProcessedFalseOrderByProviderCreationTimeAscTestIT() {
        Long providerConfigId = providerConfig.getId();

        OffsetDateTime now = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime fiveSecondsAgo = now.minusSeconds(5L);
        OffsetDateTime threeHoursAgo = now.minusHours(3L);
        OffsetDateTime twoDaysAgo = now.minusDays(2L);

        NotificationEntity firstAsc = createNotification(providerConfigId, twoDaysAgo);
        NotificationEntity secondAsc = createNotification(providerConfigId, threeHoursAgo);
        NotificationEntity thirdAsc = createNotification(providerConfigId, fiveSecondsAgo);
        NotificationEntity fourthAsc = createNotification(providerConfigId, now);

        // Insert notifications out of order
        notificationContentRepository.saveAll(List.of(secondAsc, firstAsc, fourthAsc, thirdAsc));

        int pageSize = 2;

        PageRequest firstPageRequest = PageRequest.of(0, pageSize);
        Page<NotificationEntity> firstPage = notificationContentRepository.findByProcessedFalseOrderByProviderCreationTimeAsc(firstPageRequest);
        assertPage(firstPage, pageSize);

        PageRequest secondPageRequest = PageRequest.of(1, pageSize);
        Page<NotificationEntity> secondPage = notificationContentRepository.findByProcessedFalseOrderByProviderCreationTimeAsc(secondPageRequest);
        assertPage(secondPage, pageSize);

        NotificationEntity firstElement = firstPage.stream().findFirst().orElseThrow();
        NotificationEntity lastElement = secondPage.stream().min(Comparator.comparing(NotificationEntity::getProviderCreationTime)).orElseThrow();
        assertTime(firstElement, lastElement);
    }

    @Test
    void countByProcessedTest() {
        Long providerConfigId = providerConfig.getId();

        NotificationEntity entity1 = createNotification(providerConfigId, OffsetDateTime.now(), true);
        NotificationEntity entity2 = createNotification(providerConfigId, OffsetDateTime.now(), false);
        NotificationEntity entity3 = createNotification(providerConfigId, OffsetDateTime.now(), false);

        notificationContentRepository.saveAll(List.of(entity1, entity2, entity3));

        assertEquals(1, notificationContentRepository.countByProcessed(true));
        assertEquals(2, notificationContentRepository.countByProcessed(false));
    }

    @Test
    void saveIgnoreContentIdConflictInsertsNewNotificationTest() {
        String contentId = String.format("content-id-%s", UUID.randomUUID());

        int rowsInserted = saveNotificationWithContentId(providerConfig.getId(), contentId);

        assertEquals(1, rowsInserted, "Inserting a new notification should return 1 affected row");
        assertEquals(1, notificationContentRepository.count(), "A new notification with a unique contentId should be inserted");
    }

    @Test
    void saveIgnoreContentIdConflictIgnoresDuplicateContentIdTest() {
        String contentId = String.format("content-id-%s", UUID.randomUUID());

        int firstInsert = saveNotificationWithContentId(providerConfig.getId(), contentId);
        int secondInsert = saveNotificationWithContentId(providerConfig.getId(), contentId);

        assertEquals(1, firstInsert, "Inserting a new notification should return 1 affected row");
        assertEquals(0, secondInsert, "Inserting a duplicate contentId should return 0 affected rows");
        assertEquals(1, notificationContentRepository.count(), "A duplicate contentId should not produce a second row");
    }

    @Test
    void saveIgnoreContentIdConflictSequentialIdIncrementTest() {
        int numberOfNotifications = 5;
        List<String> contentIds = new ArrayList<>();
        for (int i = 0; i < numberOfNotifications; i++) {
            String contentId = String.format("content-id-%s", UUID.randomUUID());
            contentIds.add(contentId);
            saveNotificationWithContentId(providerConfig.getId(), contentId);
        }

        List<NotificationEntity> savedNotificationEntities = notificationContentRepository.findByContentIdIn(contentIds);
        savedNotificationEntities.sort(Comparator.comparing(NotificationEntity::getId));

        assertEquals(numberOfNotifications, savedNotificationEntities.size());
        long firstId = savedNotificationEntities.get(0).getId();
        for (int i = 0; i < savedNotificationEntities.size(); i++) {
            assertEquals(firstId + i, savedNotificationEntities.get(i).getId(), "Notification IDs should be sequential starting from the first inserted ID");
        }
    }

    @Test
    void findByContentIdInReturnsMatchingEntitiesTest() {
        OffsetDateTime now = OffsetDateTime.now();

        NotificationEntity first = createNotification(providerConfig.getId(), now);
        NotificationEntity second = createNotification(providerConfig.getId(), now);
        notificationContentRepository.saveAll(List.of(first, second));

        List<NotificationEntity> result = notificationContentRepository.findByContentIdIn(List.of(first.getContentId(), second.getContentId()));

        assertEquals(2, result.size(), "Both saved notifications should be returned when queried by their contentIds");
    }

    @Test
    void findByContentIdInReturnsEmptyWhenNoMatchesTest() {
        List<NotificationEntity> result = notificationContentRepository.findByContentIdIn(
            List.of(String.format("non-existent-content-id-%s", UUID.randomUUID()), String.format("non-existent-content-id-%s", UUID.randomUUID()))
        );

        assertTrue(result.isEmpty(), "No entities should be returned when none of the contentIds exist in the database");
    }

    @Test
    void findByContentIdInReturnsSubsetForPartialMatchesTest() {
        NotificationEntity saved = createNotification(providerConfig.getId(), OffsetDateTime.now());
        notificationContentRepository.save(saved);

        List<NotificationEntity> result = notificationContentRepository.findByContentIdIn(
            List.of(saved.getContentId(), String.format("non-existent-content-id-%s", UUID.randomUUID()))
        );

        assertEquals(1, result.size(), "Only the notification with a matching contentId should be returned");
        assertEquals(saved.getContentId(), result.get(0).getContentId(), "The returned entity should match the saved contentId");
    }

    @Test
    void existsByProviderConfigIdAndMappingToJobsFalseReturnsTrueTest() {
        UUID batchId = UUID.randomUUID();
        saveNotificationInBatch(providerConfig.getId(), batchId, false, false);

        assertTrue(
            notificationContentRepository.existsByProviderConfigIdAndMappingToJobsFalse(providerConfig.getId(), batchId),
            "Should return true when an unmapped, unprocessed notification exists in the batch"
        );
    }

    @Test
    void existsByProviderConfigIdAndMappingToJobsFalseReturnsTrueWithMultipleNotificationsInBatchTest() {
        UUID batchId = UUID.randomUUID();
        saveNotificationInBatch(providerConfig.getId(), batchId, false, false);
        saveNotificationInBatch(providerConfig.getId(), batchId, false, false);

        assertTrue(
            notificationContentRepository.existsByProviderConfigIdAndMappingToJobsFalse(providerConfig.getId(), batchId),
            "Should return true when multiple unmapped, unprocessed notifications exist in the batch"
        );
    }

    @Test
    void existsByProviderConfigIdAndMappingToJobsFalseReturnsFalseWhenTableIsEmptyTest() {
        assertFalse(
            notificationContentRepository.existsByProviderConfigIdAndMappingToJobsFalse(providerConfig.getId(), UUID.randomUUID()),
            "Should return false when no notifications exist"
        );
    }

    @ParameterizedTest
    @MethodSource("mappedOrProcessedNotificationParameters")
    void existsByProviderConfigIdAndMappingToJobsFalseReturnsFalseWhenNotificationIsMappedOrProcessedTest(boolean mappingToJobs, boolean processed) {
        UUID batchId = UUID.randomUUID();
        saveNotificationInBatch(providerConfig.getId(), batchId, mappingToJobs, processed);

        assertFalse(
            notificationContentRepository.existsByProviderConfigIdAndMappingToJobsFalse(providerConfig.getId(), batchId),
            "Should return false when notification is already mapped or processed"
        );
    }

    @Test
    void existsByProviderConfigIdAndMappingToJobsFalseReturnsFalseWhenBatchIdDoesNotMatchTest() {
        UUID batchId = UUID.randomUUID();
        saveNotificationInBatch(providerConfig.getId(), batchId, false, false);

        assertFalse(
            notificationContentRepository.existsByProviderConfigIdAndMappingToJobsFalse(providerConfig.getId(), UUID.randomUUID()),
            "Should return false when queried with a different batchId"
        );
    }

    @Test
    void existsByProviderConfigIdAndMappingToJobsFalseReturnsFalseWhenProviderIdDoesNotMatchTest() {
        UUID batchId = UUID.randomUUID();
        saveNotificationInBatch(providerConfig.getId(), batchId, false, false);

        assertFalse(
            notificationContentRepository.existsByProviderConfigIdAndMappingToJobsFalse(providerConfig.getId() + 1, batchId),
            "Should return false when queried with a different providerId"
        );
    }

    private static Stream<Arguments> mappedOrProcessedNotificationParameters() {
        return Stream.of(
            Arguments.of(true, false),
            Arguments.of(false, true),
            Arguments.of(true, true)
        );
    }

    private void saveNotificationInBatch(long providerConfigId, UUID batchId, boolean mappingToJobs, boolean processed) {
        String contentId = String.format("content-id-%s", UUID.randomUUID());
        notificationContentRepository.saveIgnoreContentIdConflict(
            OffsetDateTime.now(), BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), providerConfigId,
            OffsetDateTime.now(), NotificationType.VULNERABILITY.name(), "{\"content\": {}}", processed, contentId, mappingToJobs
        );
        NotificationEntity saved = notificationContentRepository.findByContentIdIn(List.of(contentId)).get(0);
        notificationBatchRepository.save(new NotificationBatchEntity(providerConfigId, batchId, saved.getId()));
    }

    private int saveNotificationWithContentId(Long providerConfigId, String contentId) {
        return notificationContentRepository.saveIgnoreContentIdConflict(
            OffsetDateTime.now(),
            BLACK_DUCK_PROVIDER_KEY.getUniversalKey(),
            providerConfigId,
            OffsetDateTime.now(),
            NotificationType.VULNERABILITY.name(),
            "{\"content\": {}}",
            false,
            contentId,
            false
        );
    }

    private DescriptorConfigEntity createProviderConfig() {
        RegisteredDescriptorEntity providerDescriptor = registeredDescriptorRepository.findFirstByName(BLACK_DUCK_PROVIDER_KEY.getUniversalKey()).orElseThrow();
        ConfigContextEntity context = configContextRepository.findFirstByContext(ConfigContextEnum.GLOBAL.name()).orElseThrow();
        DescriptorConfigEntity providerConfigToSave = new DescriptorConfigEntity(providerDescriptor.getId(), context.getId(), OffsetDateTime.now(), OffsetDateTime.now());
        return descriptorConfigRepository.save(providerConfigToSave);
    }

    private NotificationEntity createNotification(Long providerConfigId, OffsetDateTime providerCreationTime) {
        return createNotification(providerConfigId, providerCreationTime, false);
    }

    private NotificationEntity createNotification(Long providerConfigId, OffsetDateTime providerCreationTime, boolean processed) {
        return new NotificationEntity(
            OffsetDateTime.now(),
            BLACK_DUCK_PROVIDER_KEY.getUniversalKey(),
            providerConfigId,
            providerCreationTime,
            NotificationType.VULNERABILITY.name(),
            "{\"content\": {}}",
            processed,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
    }

    private static void assertPage(Page<NotificationEntity> page, int pageSize) {
        assertEquals(pageSize, page.getSize());
        List<NotificationEntity> pageContent = page.getContent();
        NotificationEntity firstElement = pageContent.get(0);
        NotificationEntity secondElement = pageContent.get(1);
        assertTime(firstElement, secondElement);
    }

    private static void assertTime(NotificationEntity olderNotification, NotificationEntity newerNotification) {
        assertTrue(olderNotification.getProviderCreationTime().isBefore(newerNotification.getProviderCreationTime()), "Expected notifications to be sorted from oldest to newest");
    }

}
