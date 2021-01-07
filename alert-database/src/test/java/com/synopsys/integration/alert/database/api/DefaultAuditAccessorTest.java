package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
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

public class DefaultAuditAccessorTest {

    private final Gson gson = new Gson();

    @Test
    public void findMatchingAuditIdTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findMatchingAudit(Mockito.anyLong(), Mockito.any(UUID.class))).thenReturn(Optional.empty());
        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, null, null, null, null, null);
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
        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, null, null, null, null, contentConverter);

        assertFalse(auditUtility.findFirstByJobId(UUID.randomUUID()).isPresent());
    }

    @Test
    public void findFirstByJobIdNullTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(null, null, null, null, null, null);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any(UUID.class))).thenReturn(Optional.of(auditEntryEntity));

        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, null, null, null, null, contentConverter);

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

        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, null, null, null, null, null);
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

        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, auditNotificationRepository, null, null, notificationManager, null);
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
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

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
                                                   .notificationTypes(List.of())
                                                   .processingType(ProcessingType.DEFAULT)
                                                   .build();
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJob));

        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, auditNotificationRepository, jobAccessor, configurationAccessor, null, contentConverter);
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

    @Test
    public void createAuditEntryTest() throws Exception {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, auditNotificationRepository, null, null, null, null);
        ProviderMessageContent content = createMessageContent();
        UUID commonConfigUUID = UUID.randomUUID();
        AuditEntryEntity savedAuditEntryEntity = new AuditEntryEntity(commonConfigUUID, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.SUCCESS.toString(), null, null);
        final Long auditID = 10L;
        savedAuditEntryEntity.setId(auditID);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(savedAuditEntryEntity));
        mockAuditRepositorySave(auditEntryRepository, savedAuditEntryEntity);

        Map<Long, Long> existingNotificationIdToAuditId = new HashMap<>();
        existingNotificationIdToAuditId.put(1L, auditID);
        Map<Long, Long> savedNotificationIdToAuditId = auditUtility.createAuditEntry(existingNotificationIdToAuditId, commonConfigUUID, MessageContentGroup.singleton(content));
        assertFalse(savedNotificationIdToAuditId.isEmpty());
        assertEquals(2, savedNotificationIdToAuditId.size());
        assertEquals(savedAuditEntryEntity.getId(), savedNotificationIdToAuditId.get(1L));
        assertEquals(AuditEntryStatus.PENDING.toString(), savedAuditEntryEntity.getStatus());
        Mockito.verify(auditEntryRepository).findById(Mockito.anyLong());
        Mockito.verify(auditNotificationRepository, Mockito.times(existingNotificationIdToAuditId.size())).save(Mockito.any(AuditNotificationRelation.class));
    }

    @Test
    public void createAuditEntryNullEntryIdTest() throws Exception {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, auditNotificationRepository, null, null, null, null);
        ProviderMessageContent content = createMessageContent();
        UUID commonConfigUUID = UUID.randomUUID();
        AuditEntryEntity savedAuditEntryEntity = new AuditEntryEntity(commonConfigUUID, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.SUCCESS.toString(), null, null);
        savedAuditEntryEntity.setId(10L);

        mockAuditRepositorySave(auditEntryRepository, savedAuditEntryEntity);
        Map<Long, Long> savedNotificationIdToAuditId = auditUtility.createAuditEntry(null, commonConfigUUID, MessageContentGroup.singleton(content));
        assertFalse(savedNotificationIdToAuditId.isEmpty());
        assertEquals(2, savedNotificationIdToAuditId.size());
        assertEquals(savedAuditEntryEntity.getId(), savedNotificationIdToAuditId.values().iterator().next());
        assertEquals(AuditEntryStatus.PENDING.toString(), savedAuditEntryEntity.getStatus());
        Mockito.verify(auditEntryRepository, Mockito.times(0)).findById(Mockito.anyLong());
        Mockito.verify(auditNotificationRepository, Mockito.times(2)).save(Mockito.any(AuditNotificationRelation.class));
    }

    @Test
    public void setAuditEntrySuccessCatchExceptionTest() {
        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(null, null, null, null, null, null);
        auditUtility.setAuditEntrySuccess(Collections.singletonList(1L));
    }

    @Test
    public void setAuditEntrySuccessTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, null, null, null, null, null);

        AuditEntryEntity entity = new AuditEntryEntity(UUID.randomUUID(), DateUtils.createCurrentDateTimestamp().minusSeconds(1), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.SUCCESS.toString(), null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntrySuccess(Collections.emptyList());
        auditUtility.setAuditEntrySuccess(Collections.singletonList(entity.getId()));
        assertEquals(AuditEntryStatus.SUCCESS.toString(), entity.getStatus());
    }

    @Test
    public void setAuditEntryFailureCatchExceptionTest() {
        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(null, null, null, null, null, null);
        auditUtility.setAuditEntryFailure(Collections.singletonList(1L), null, null);
    }

    @Test
    public void setAuditEntryFailureTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        DefaultAuditAccessor auditUtility = new DefaultAuditAccessor(auditEntryRepository, null, null, null, null, null);
        AuditEntryEntity entity = new AuditEntryEntity(UUID.randomUUID(), DateUtils.createCurrentDateTimestamp().minusSeconds(1), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.FAILURE.toString(), null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntryFailure(Collections.emptyList(), null, null);
        auditUtility.setAuditEntryFailure(Collections.singletonList(entity.getId()), "error", new Exception());
        assertEquals(AuditEntryStatus.FAILURE.toString(), entity.getStatus());
        assertEquals("error", entity.getErrorMessage());
    }

    public ProviderMessageContent createMessageContent() throws AlertException {
        LinkableItem linkableItem1 = new LinkableItem("First Linkable Item", "Value 1", "https://google.com");
        LinkableItem linkableItem2 = new LinkableItem("Second Linkable Item", "Value 2", "https://google.com");

        final String nameKey = "Same Key";
        LinkableItem linkableItem3 = new LinkableItem(nameKey, "Value", "https://google.com");
        LinkableItem linkableItem4 = new LinkableItem(nameKey, "No Link Value");
        LinkableItem linkableItem5 = new LinkableItem(nameKey, "Other Value", "https://google.com");

        final String category = "category";

        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyCategory(category)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyComponentData("component", "component_1")
                                            .applyCategoryItem("category item", "category_item_1")
                                            .applyNotificationId(1L)
                                            .applyComponentAttribute(linkableItem1)
                                            .applyComponentAttribute(linkableItem2)
                                            .build();
        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyCategory(category)
                                            .applyOperation(ItemOperation.UPDATE)
                                            .applyNotificationId(2L)
                                            .applyComponentData("component", "component_2")
                                            .applyCategoryItem("category item", "category_item_2")
                                            .applyComponentAttribute(linkableItem2)
                                            .build();
        ComponentItem componentItem_3 = new ComponentItem.Builder()
                                            .applyCategory(category)
                                            .applyOperation(ItemOperation.DELETE)
                                            .applyNotificationId(1L)
                                            .applyComponentData("component", "component_1")
                                            .applyCategoryItem("category item", "category_item_1")
                                            .applyComponentAttribute(linkableItem3)
                                            .applyComponentAttribute(linkableItem4)
                                            .applyComponentAttribute(linkableItem5)
                                            .build();

        LinkableItem subTopic = new LinkableItem("Sub Topic", "Sub Topic Value", "https://google.com");

        return new ProviderMessageContent.Builder()
                   .applyProvider("provider", 1L, "providerConfig")
                   .applyProject("Topic", "audit utility test", "https://google.com")
                   .applyProjectVersion(subTopic.getName(), subTopic.getValue(), subTopic.getUrl().orElse(null))
                   .applyAllComponentItems(List.of(componentItem_1, componentItem_2, componentItem_3))
                   .build();
    }

    private void mockAuditRepositorySave(AuditEntryRepository auditEntryRepository, AuditEntryEntity savedAuditEntryEntity) {
        Mockito.when(auditEntryRepository.save(Mockito.any(AuditEntryEntity.class))).then(invocation -> {
            AuditEntryEntity originalEntity = invocation.getArgument(0);
            if (null != originalEntity.getId()) {
                savedAuditEntryEntity.setId(originalEntity.getId());
            }
            savedAuditEntryEntity.setStatus(originalEntity.getStatus());
            savedAuditEntryEntity.setErrorMessage(originalEntity.getErrorMessage());
            savedAuditEntryEntity.setErrorStackTrace(originalEntity.getErrorStackTrace());
            savedAuditEntryEntity.setTimeLastSent(originalEntity.getTimeLastSent());
            return savedAuditEntryEntity;
        });
    }

}
