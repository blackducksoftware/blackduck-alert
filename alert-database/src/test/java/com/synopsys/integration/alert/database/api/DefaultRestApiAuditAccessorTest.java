package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class DefaultRestApiAuditAccessorTest {
    private final Gson gson = new Gson();

    @Test
    public void findMatchingAuditIdTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findMatchingAudit(Mockito.anyLong(), Mockito.any(UUID.class))).thenReturn(Optional.empty());
        DefaultRestApiAuditAccessor auditUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, null, null, null, null, null);
        Optional<Long> nullValue = auditUtility.findMatchingAuditId(1L, UUID.randomUUID());

        assertFalse(nullValue.isPresent());

        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(null, null, null, null, null, null);
        Long expectedLong = 2L;
        auditEntryEntity.setId(expectedLong);
        Mockito.when(auditEntryRepository.findMatchingAudit(Mockito.anyLong(), Mockito.any(UUID.class))).thenReturn(Optional.of(auditEntryEntity));
        Optional<Long> actualValue = auditUtility.findMatchingAuditId(expectedLong, UUID.randomUUID());

        assertTrue(actualValue.isPresent());
        assertEquals(expectedLong, actualValue.get());
    }

    @Test
    public void findFirstByJobIdEmptyTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any(UUID.class))).thenReturn(Optional.empty());
        DefaultRestApiAuditAccessor auditUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, null, null, null, null, contentConverter);

        assertFalse(auditUtility.findFirstByJobId(UUID.randomUUID()).isPresent());
    }

    @Test
    public void findFirstByJobIdNullTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(null, null, null, null, null, null);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any(UUID.class))).thenReturn(Optional.of(auditEntryEntity));

        DefaultRestApiAuditAccessor auditUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, null, null, null, null, contentConverter);

        UUID testUUID = UUID.randomUUID();
        AuditJobStatusModel auditJobStatusModel = auditUtility.findFirstByJobId(testUUID).get();
        String timeAuditCreated = auditJobStatusModel.getTimeAuditCreated();
        String timeLastSent = auditJobStatusModel.getTimeLastSent();
        String status = auditJobStatusModel.getStatus();

        assertTrue(auditUtility.findFirstByJobId(testUUID).isPresent());
        assertNull(timeAuditCreated);
        assertNull(timeLastSent);
        assertNull(status);
    }

    @Test
    public void findFirstByJobIdNotNullTest() {
        OffsetDateTime timeLastSent = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime timeCreated = timeLastSent.minusMinutes(10L);
        AuditEntryStatus status = AuditEntryStatus.PENDING;
        UUID testUUID = UUID.randomUUID();

        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(testUUID, timeCreated, timeLastSent, status.name(), null, null);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any(UUID.class))).thenReturn(Optional.of(auditEntryEntity));

        DefaultRestApiAuditAccessor auditUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, null, null, null, null, null);
        AuditJobStatusModel auditJobStatusModel = auditUtility.findFirstByJobId(testUUID).get();

        String testTimeAuditCreated = auditJobStatusModel.getTimeAuditCreated();
        String testTimeLastSent = auditJobStatusModel.getTimeLastSent();
        String testStatus = auditJobStatusModel.getStatus();

        assertTrue(auditUtility.findFirstByJobId(testUUID).isPresent());
        assertEquals(DateUtils.formatDate(timeCreated, DateUtils.AUDIT_DATE_FORMAT), testTimeAuditCreated);
        assertEquals(DateUtils.formatDate(timeLastSent, DateUtils.AUDIT_DATE_FORMAT), testTimeLastSent);
        assertEquals(status.getDisplayName(), testStatus);
    }

    @Test
    public void getPageOfAuditEntriesTest() {
        Integer pageNumber = 0;
        int pageSize = 2;
        String searchTerm = null;
        String sortField = "lastSent";
        String sortOrder = "ASC";
        Boolean onlyShowSentNotifications = Boolean.TRUE;
        String overallStatus = "overallStatusString";
        String lastSent = DateUtils.createCurrentDateString(DateUtils.AUDIT_DATE_FORMAT);

        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);

        PageRequest pageRequest = PageRequest.of(0, 10);

        Mockito.when(auditEntryRepository.findMatchingAudit(Mockito.anyLong(), Mockito.any(UUID.class))).thenReturn(Optional.empty());
        Mockito.when(notificationManager.getPageRequestForNotifications(pageNumber, pageSize, sortField, sortOrder)).thenReturn(pageRequest);

        //At least two AlertNotificationModel are required for the comparator
        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(1L, 1L, "provider-test", "providerConfigName-test", "notificationType-test", "{content: \"content is here...\"}", DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), false);
        AlertNotificationModel alertNotificationModel2 = new AlertNotificationModel(2L, 2L, "provider-test2", "providerConfigName-test2", "notificationType-test2", "{content: \"content is here2..\"}",
            DateUtils.createCurrentDateTimestamp().minusSeconds(15), DateUtils.createCurrentDateTimestamp().minusSeconds(10), false);

        Pageable auditPageable = Mockito.mock(Pageable.class);
        Mockito.when(auditPageable.getOffset()).thenReturn(pageNumber.longValue());
        Mockito.when(auditPageable.getPageSize()).thenReturn(pageSize);
        Page<AlertNotificationModel> auditPage = new PageImpl<>(List.of(alertNotificationModel, alertNotificationModel2), auditPageable, 1);
        Mockito.when(notificationManager.findAll(pageRequest, onlyShowSentNotifications)).thenReturn(auditPage);

        NotificationConfig notificationConfig = new NotificationConfig("3", "createdAtString", "providerString", 2L, "providerConfigNameString", "providerCreationTimeString", "notificationTypeString", "content-test");
        AuditEntryModel auditEntryModel = new AuditEntryModel("2", notificationConfig, List.of(), overallStatus, lastSent);
        Function<AlertNotificationModel, AuditEntryModel> notificationToAuditEntryConverter = (AlertNotificationModel notificationModel) -> auditEntryModel;

        DefaultRestApiAuditAccessor auditUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, auditNotificationRepository, null, null, notificationManager, null);
        AuditEntryPageModel alertPagedModel = auditUtility.getPageOfAuditEntries(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications, notificationToAuditEntryConverter);

        assertEquals(1, alertPagedModel.getTotalPages());
        assertEquals(pageNumber.intValue(), alertPagedModel.getCurrentPage());
        assertEquals(pageSize, alertPagedModel.getPageSize());
        assertEquals(2, alertPagedModel.getContent().size());
        assertEquals(lastSent, alertPagedModel.getContent().get(0).getLastSent());
        assertEquals(lastSent, alertPagedModel.getContent().get(1).getLastSent());

        AuditEntryModel auditContentTest = alertPagedModel.getContent().stream().findFirst().orElse(null);

        assertEquals(auditEntryModel.getId(), auditContentTest.getId());
        assertEquals(notificationConfig, auditContentTest.getNotification());
        assertEquals(0, auditContentTest.getJobs().size());
        assertEquals(overallStatus, auditContentTest.getOverallStatus());
        assertEquals(lastSent, auditContentTest.getLastSent());
    }

    @Test
    public void convertToAuditEntryModelFromNotificationTest() throws Exception {
        Long id = 1L;
        Long providerConfigId = 2L;
        String provider = "provider-test";
        String providerConfigName = "providerConfigName-test";
        String notificationType = "notificationType-test";
        String content = "content-test";
        OffsetDateTime timeLastSent = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime timeCreated = timeLastSent.minusSeconds(10);
        Long auditEntryId = 3L;
        String channelName = "test-channel.common.name-value";
        String eventType = "test-channel.common.channel.name-value";

        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        ContentConverter contentConverter = new ContentConverter(new DefaultConversionService());

        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(id, providerConfigId, provider, providerConfigName, notificationType, content, DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), false);
        AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(auditEntryId, alertNotificationModel.getId());
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(UUID.randomUUID(), timeCreated, timeLastSent, AuditEntryStatus.SUCCESS.name(), null, null);

        Mockito.when(auditNotificationRepository.findByNotificationId(Mockito.any())).thenReturn(List.of(auditNotificationRelation));
        Mockito.when(auditEntryRepository.findAllById(Mockito.any())).thenReturn(List.of(auditEntryEntity));

        DistributionJobModel distributionJob = DistributionJobModel.builder()
                                                   .jobId(UUID.randomUUID())
                                                   .enabled(true)
                                                   .blackDuckGlobalConfigId(2L)
                                                   .channelDescriptorName("test-channel.common.channel.name-value")
                                                   .name("test-channel.common.name-value")
                                                   .distributionFrequency(FrequencyType.REAL_TIME)
                                                   .filterByProject(false)
                                                   .notificationTypes(List.of(NotificationType.LICENSE_LIMIT.name()))
                                                   .processingType(ProcessingType.DEFAULT)
                                                   .createdAt(DateUtils.createCurrentDateTimestamp())
                                                   .build();
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJob));

        DefaultRestApiAuditAccessor auditUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, auditNotificationRepository, jobAccessor, configurationModelConfigurationAccessor, null, contentConverter);
        AuditEntryModel testAuditEntryModel = auditUtility.convertToAuditEntryModelFromNotification(alertNotificationModel);

        assertEquals(id, Long.valueOf(testAuditEntryModel.getId()));
        assertNotNull(testAuditEntryModel.getNotification());
        assertFalse(testAuditEntryModel.getJobs().isEmpty());
        assertEquals(1, testAuditEntryModel.getJobs().size());
        JobAuditModel testJob = testAuditEntryModel.getJobs().get(0);
        assertEquals(channelName, testJob.getName());
        assertEquals(eventType, testJob.getEventType());
        assertEquals(AuditEntryStatus.SUCCESS.getDisplayName(), testAuditEntryModel.getOverallStatus());
        assertEquals(DateUtils.formatDate(timeLastSent, DateUtils.AUDIT_DATE_FORMAT), testAuditEntryModel.getLastSent());
    }

}
