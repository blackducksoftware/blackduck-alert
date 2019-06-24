package com.synopsys.integration.alert.database.settings.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.rest.RestConstants;

@Transactional
public class NotificationContentRepositoryIT extends AlertIntegrationTest {
    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private AuditEntryRepository auditEntryRepository;
    @Autowired
    private DefaultAuditUtility defaultAuditUtility;
    @Autowired
    private DefaultConfigurationAccessor defaultConfigurationAccessor;

    @BeforeEach
    public void init() {
        notificationContentRepository.deleteAllInBatch();
        auditEntryRepository.deleteAllInBatch();
        notificationContentRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        notificationContentRepository.deleteAllInBatch();
        auditEntryRepository.deleteAllInBatch();
    }

    @Test
    public void testSaveEntity() throws Exception {
        final NotificationContent entity = createEntity(RestConstants.formatDate(new Date()));
        final NotificationContent savedEntity = notificationContentRepository.save(entity);
        final long count = notificationContentRepository.count();
        assertEquals(1, count);
        final Optional<NotificationContent> foundEntityOptional = notificationContentRepository.findById(savedEntity.getId());
        final NotificationContent foundEntity = foundEntityOptional.get();
        assertEquals(entity.getCreatedAt(), foundEntity.getCreatedAt());
        assertEquals(entity.getNotificationType(), foundEntity.getNotificationType());
        assertEquals(entity.getProvider(), foundEntity.getProvider());
        assertEquals(entity.getProviderCreationTime(), foundEntity.getProviderCreationTime());
        assertEquals(entity.getContent(), foundEntity.getContent());
    }

    @Test
    public void testFindByDate() throws Exception {
        final Set<String> validResultDates = new HashSet<>();
        NotificationContent savedEntity = createEntity("2017-10-15T1:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-21T14:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-22T14:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-23T14:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-30T14:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));

        createEntity("2017-10-10T16:00:00.000Z");
        createEntity("2017-10-31T15:00:00.000Z");
        createEntity("2017-10-31T16:00:00.000Z");
        createEntity("2017-10-31T17:00:00.000Z");
        createEntity("2017-10-31T18:00:00.000Z");
        final long count = notificationContentRepository.count();
        assertEquals(10, count);
        final Date startDate = RestConstants.parseDateString("2017-10-12T01:30:59.000Z");
        final Date endDate = RestConstants.parseDateString("2017-10-30T16:59:59.000Z");
        final List<NotificationContent> foundEntityList = notificationContentRepository.findByCreatedAtBetween(startDate, endDate);
        assertEquals(5, foundEntityList.size());

        foundEntityList.forEach(entity -> {
            final String createdAtString = RestConstants.formatDate(entity.getCreatedAt());
            assertTrue(validResultDates.contains(createdAtString));
        });
    }

    // Only re-enable for performance testing
    @Test
    @Disabled
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.DEFAULT_PERFORMANCE),
        @Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
    })
    public void findMatchingNotificationTest() throws ParseException, AlertException {
        notificationQueryTest(notificationContentRepository::findMatchingNotification);
    }

    // Only re-enable for performance testing
    @Test
    @Disabled
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.DEFAULT_PERFORMANCE),
        @Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
    })
    public void findMatchingSentNotificationTest() throws ParseException, AlertException {
        notificationQueryTest(notificationContentRepository::findMatchingSentNotification);
    }

    public void notificationQueryTest(final BiFunction<String, Pageable, Page<NotificationContent>> queryFunction) throws ParseException, AlertException {
        final String searchTerm = "searchTerm";
        final int numberToCreate = 1000;
        final Number numberOfSearchTermMatches = initializeNotificationRepo(searchTerm, numberToCreate);

        final Instant beforeQueryInstant = Instant.now();
        final Page<NotificationContent> matchingNotifications = queryFunction.apply(searchTerm, Pageable.unpaged());
        final Instant afterQueryInstant = Instant.now();

        final Duration queryDuration = Duration.between(beforeQueryInstant, afterQueryInstant);
        final Long durationInSeconds = queryDuration.toSeconds();
        System.out.println("Duration (in seconds): " + durationInSeconds);

        assertEquals(numberOfSearchTermMatches, matchingNotifications.getTotalElements());
    }

    private Long initializeNotificationRepo(final String searchTerm, final int numberToCreate) throws ParseException, AlertException {
        final List<NotificationContent> notifications = new ArrayList<>(numberToCreate);
        long searchableCount = 0;

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);

        for (int i = 0; i < numberToCreate; i++) {
            final Date newDate = new Date();
            final String dateString = simpleDateFormat.format(newDate);

            final NotificationContent entity;
            if (i % 31 == 0) {
                entity = createEntity(dateString, searchTerm);
                searchableCount++;
            } else {
                entity = createEntity(dateString);
            }
            notifications.add(entity);
        }

        final List<NotificationContent> savedNotifications = notificationContentRepository.saveAll(notifications);

        final ConfigurationFieldModel fieldModel = ConfigurationFieldModel.create(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT);
        fieldModel.setFieldValue("false");
        final ConfigurationJobModel configJob = defaultConfigurationAccessor.createJob(Set.of(BlackDuckProvider.COMPONENT_NAME), Set.of(fieldModel));

        for (final NotificationContent notification : savedNotifications) {
            final MessageContentGroup messageContentGroup = createMessageGroup(notification.getId());
            defaultAuditUtility.createAuditEntry(Map.of(), configJob.getJobId(), messageContentGroup);
        }

        auditEntryRepository.flush();

        return searchableCount;
    }

    private NotificationContent createEntity(final String dateString) throws ParseException {
        return createEntity(dateString, "NOTIFICATION CONTENT HERE");
    }

    private NotificationContent createEntity(final String dateString, final String content) throws ParseException {
        final Date createdAt = RestConstants.parseDateString(dateString);
        final Date providerCreationTime = createdAt;
        final String provider = "provider_1";
        final String notificationType = "type_1";
        final NotificationContent entity = new MockNotificationContent(createdAt, provider, providerCreationTime, notificationType, content, null).createEntity();
        final NotificationContent savedEntity = notificationContentRepository.save(entity);
        return savedEntity;
    }

    private MessageContentGroup createMessageGroup(final Long notificationId) throws AlertException {
        ComponentItem componentItem = new ComponentItem.Builder()
                                          .applyComponentData("", "")
                                          .applyOperation(ItemOperation.UPDATE)
                                          .applyNotificationId(notificationId)
                                          .build();
        final ProviderMessageContent content = new ProviderMessageContent.Builder()
                                                   .applyProvider("testProvider")
                                                   .applyTopic("testTopic", "")
                                                   .applyAllComponentItems(List.of(componentItem))
                                                   .build();
        return MessageContentGroup.singleton(content);
    }

}
