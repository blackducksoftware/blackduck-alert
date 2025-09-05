/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

    @AfterEach
    public void cleanup() {
        notificationContentRepository.deleteAll();
    }

    @Test
    @Transactional
    void findByProcessedFalseOrderByProviderCreationTimeAscTestIT() {
        // Create provider config (required for FK constraint)
        DescriptorConfigEntity providerConfig = createProviderConfig();
        Long providerConfigId = providerConfig.getId();

        // Create notifications
        OffsetDateTime now = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime fiveSecondsAgo = now.minusSeconds(5L);
        OffsetDateTime threeHoursAgo = now.minusHours(3L);
        OffsetDateTime twoDaysAgo = now.minusDays(2L);

        NotificationEntity firstAsc = createNotification(providerConfigId, twoDaysAgo);
        NotificationEntity secondAsc = createNotification(providerConfigId, threeHoursAgo);
        NotificationEntity thirdAsc = createNotification(providerConfigId, fiveSecondsAgo);
        NotificationEntity fourthAsc = createNotification(providerConfigId, now);

        // Begin test...

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
    @Transactional
    void countByProcessedTest() {
        DescriptorConfigEntity providerConfig = createProviderConfig();
        Long providerConfigId = providerConfig.getId();

        NotificationEntity entity1 = createNotification(providerConfigId, OffsetDateTime.now(), true);
        NotificationEntity entity2 = createNotification(providerConfigId, OffsetDateTime.now(), false);
        NotificationEntity entity3 = createNotification(providerConfigId, OffsetDateTime.now(), false);

        notificationContentRepository.saveAll(List.of(entity1, entity2, entity3));

        assertEquals(1, notificationContentRepository.countByProcessed(true));
        assertEquals(2, notificationContentRepository.countByProcessed(false));
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
