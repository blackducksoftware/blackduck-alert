package com.synopsys.integration.alert.database.settings.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.rest.RestConstants;

@Transactional
@AlertIntegrationTest
public class NotificationContentRepositoryIT {
    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private AuditEntryRepository auditEntryRepository;
    @Autowired
    private AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private JobAccessor jobAccessor;
    @Autowired
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    private ConfigurationModel providerConfigModel = null;

    @BeforeEach
    public void init() {
        notificationContentRepository.deleteAllInBatch();
        auditEntryRepository.deleteAllInBatch();
        notificationContentRepository.flush();
        auditEntryRepository.flush();

        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("true");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue("My Black Duck Config");

        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue("https://a-blackduck-server");
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue("123456789012345678901234567890123456789012345678901234567890");
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue("300");

        List<ConfigurationFieldModel> providerConfigFields = List.of(providerConfigEnabled, providerConfigName, blackduckUrl, blackduckApiKey, blackduckTimeout);
        providerConfigModel = configurationModelConfigurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, providerConfigFields);
    }

    @AfterEach
    public void cleanup() {
        configurationModelConfigurationAccessor.deleteConfiguration(providerConfigModel.getConfigurationId());

        notificationContentRepository.deleteAllInBatch();
        auditEntryRepository.deleteAllInBatch();
    }

    @Test
    public void testSaveEntity() throws Exception {
        NotificationEntity entity = createEntity(RestConstants.formatDate(new Date()));
        NotificationEntity savedEntity = notificationContentRepository.save(entity);
        long count = notificationContentRepository.count();
        assertEquals(1, count);
        Optional<NotificationEntity> foundEntityOptional = notificationContentRepository.findById(savedEntity.getId());
        NotificationEntity foundEntity = foundEntityOptional.get();
        assertEquals(entity.getCreatedAt(), foundEntity.getCreatedAt());
        assertEquals(entity.getNotificationType(), foundEntity.getNotificationType());
        assertEquals(entity.getProvider(), foundEntity.getProvider());
        assertEquals(entity.getProviderCreationTime(), foundEntity.getProviderCreationTime());
        assertEquals(entity.getContent(), foundEntity.getContent());
    }

    @Test
    public void testFindByDate() throws Exception {
        Set<String> validResultDates = new HashSet<>();
        NotificationEntity savedEntity = createEntity("2017-10-15T1:00:00.000Z");
        validResultDates.add(DateUtils.formatDateAsJsonString(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-21T14:00:00.000Z");
        validResultDates.add(DateUtils.formatDateAsJsonString(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-22T14:00:00.000Z");
        validResultDates.add(DateUtils.formatDateAsJsonString(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-23T14:00:00.000Z");
        validResultDates.add(DateUtils.formatDateAsJsonString(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-30T14:00:00.000Z");
        validResultDates.add(DateUtils.formatDateAsJsonString(savedEntity.getCreatedAt()));

        createEntity("2017-10-10T16:00:00.000Z");
        createEntity("2017-10-31T15:00:00.000Z");
        createEntity("2017-10-31T16:00:00.000Z");
        createEntity("2017-10-31T17:00:00.000Z");
        createEntity("2017-10-31T18:00:00.000Z");
        long count = notificationContentRepository.count();
        assertEquals(10, count);
        OffsetDateTime startDate = DateUtils.parseDate("2017-10-12T01:30:59.000Z", RestConstants.JSON_DATE_FORMAT);
        OffsetDateTime endDate = DateUtils.parseDate("2017-10-30T16:59:59.000Z", RestConstants.JSON_DATE_FORMAT);
        PageRequest pageRequest = PageRequest.of(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE);
        List<NotificationEntity> foundEntityList = notificationContentRepository.findByCreatedAtBetween(startDate, endDate, pageRequest).stream()
            .collect(Collectors.toList());
        assertEquals(5, foundEntityList.size());

        foundEntityList.forEach(entity -> {
            String createdAtString = DateUtils.formatDateAsJsonString(entity.getCreatedAt());
            assertTrue(validResultDates.contains(createdAtString));
        });
    }

    @Test
    @Disabled("Only re-enable for performance testing")
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.DEFAULT_PERFORMANCE),
        @Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
    })
    public void findMatchingNotificationTest() throws ParseException, AlertException {
        notificationQueryTest(notificationContentRepository::findMatchingNotification);
    }

    @Test
    @Disabled("Only re-enable for performance testing")
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.DEFAULT_PERFORMANCE),
        @Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
    })
    public void findMatchingSentNotificationTest() throws ParseException, AlertException {
        notificationQueryTest(notificationContentRepository::findMatchingSentNotification);
    }

    public void notificationQueryTest(BiFunction<String, Pageable, Page<NotificationEntity>> queryFunction) throws ParseException, AlertException {
        final String searchTerm = "searchTerm";
        final int numberToCreate = 1000;
        Number numberOfSearchTermMatches = initializeNotificationRepo(searchTerm, numberToCreate);

        Instant beforeQueryInstant = Instant.now();
        Page<NotificationEntity> matchingNotifications = queryFunction.apply(searchTerm, Pageable.unpaged());
        Instant afterQueryInstant = Instant.now();

        Duration queryDuration = Duration.between(beforeQueryInstant, afterQueryInstant);
        Long durationInSeconds = queryDuration.toSeconds();
        System.out.println("Duration (in seconds): " + durationInSeconds);

        assertEquals(numberOfSearchTermMatches, matchingNotifications.getTotalElements());
    }

    private Long initializeNotificationRepo(String searchTerm, int numberToCreate) throws ParseException {
        List<NotificationEntity> notifications = new ArrayList<>(numberToCreate);
        long searchableCount = 0;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);

        for (int i = 0; i < numberToCreate; i++) {
            Date newDate = new Date();
            String dateString = simpleDateFormat.format(newDate);

            NotificationEntity entity;
            if (i % 31 == 0) {
                entity = createEntity(dateString, searchTerm);
                searchableCount++;
            } else {
                entity = createEntity(dateString);
            }
            notifications.add(entity);
        }

        List<NotificationEntity> savedNotifications = notificationContentRepository.saveAll(notifications);

        DistributionJobRequestModel jobRequestModel = createJobRequestModel();
        DistributionJobModel jobModel = jobAccessor.createJob(jobRequestModel);
        UUID jobId = jobModel.getJobId();

        // TODO gk: these tests were not working before I removed the old code
        //  more investigation is needed
        for (NotificationEntity savedNotification : savedNotifications) {
            AuditEntryEntity newEntry = new AuditEntryEntity(jobId, OffsetDateTime.now(), null, AuditEntryStatus.PENDING.name(), null, null);
            AuditEntryEntity savedEntry = auditEntryRepository.save(newEntry);

            AuditNotificationRelation notifAudit = new AuditNotificationRelation(savedEntry.getId(), savedNotification.getId());
            auditNotificationRepository.save(notifAudit);
        }

        auditEntryRepository.flush();

        return searchableCount;
    }

    private NotificationEntity createEntity(String dateString) throws ParseException {
        return createEntity(dateString, "NOTIFICATION CONTENT HERE");
    }

    private NotificationEntity createEntity(String dateString, String content) throws ParseException {
        OffsetDateTime providerCreationTime = DateUtils.parseDateFromJsonString(dateString);
        final String provider = "provider_blackduck";
        final String notificationType = "type_1";
        NotificationEntity entity = new MockNotificationContent(providerCreationTime, provider, providerCreationTime, notificationType, content, null, providerConfigModel.getConfigurationId()).createEntity();
        NotificationEntity savedEntity = notificationContentRepository.save(entity);
        return savedEntity;
    }

    private DistributionJobRequestModel createJobRequestModel() {
        SlackJobDetailsModel details = new SlackJobDetailsModel(null, "test_webhook", "#test-channel", null);
        return new DistributionJobRequestModel(
            true,
            "Test Slack Job",
            FrequencyType.REAL_TIME,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            providerConfigModel.getConfigurationId(),
            false,
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            details
        );
    }

}
