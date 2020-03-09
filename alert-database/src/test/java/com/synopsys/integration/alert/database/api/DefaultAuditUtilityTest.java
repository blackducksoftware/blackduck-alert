package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;

public class DefaultAuditUtilityTest {

    @Test
    public void findMatchingAuditIdTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findMatchingAudit(Mockito.anyLong(), Mockito.any(UUID.class))).thenReturn(Optional.empty());
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, null);
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
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, contentConverter);

        assertFalse(auditUtility.findFirstByJobId(UUID.randomUUID()).isPresent());
    }

    @Test
    public void findFirstByJobIdNullTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(null, null, null, null, null, null);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any(UUID.class))).thenReturn(Optional.of(auditEntryEntity));

        DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, contentConverter);

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
        Date timeCreated = Date.from(Instant.now().minusSeconds(600L));
        Date timeLastSent = Date.from(Instant.now());
        AuditEntryStatus status = AuditEntryStatus.PENDING;
        UUID testUUID = UUID.randomUUID();

        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(testUUID, timeCreated, timeLastSent, status.name(), null, null);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any(UUID.class))).thenReturn(Optional.of(auditEntryEntity));
        Mockito.when(contentConverter.getStringValue(Mockito.eq(timeCreated))).thenReturn(timeCreated.toString());
        Mockito.when(contentConverter.getStringValue(Mockito.eq(timeLastSent))).thenReturn(timeLastSent.toString());

        DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, contentConverter);
        AuditJobStatusModel auditJobStatusModel = auditUtility.findFirstByJobId(testUUID).get();

        String testTimeAuditCreated = auditJobStatusModel.getTimeAuditCreated();
        String testTimeLastSent = auditJobStatusModel.getTimeLastSent();
        String testStatus = auditJobStatusModel.getStatus();

        assertTrue(auditUtility.findFirstByJobId(testUUID).isPresent());
        assertEquals(timeCreated.toString(), testTimeAuditCreated);
        assertEquals(timeLastSent.toString(), testTimeLastSent);
        assertEquals(status.getDisplayName(), testStatus);
    }

    @Test
    public void getPageOfAuditEntriesTest() {
        Integer pageNumber = 1;
        int pageSize = 1;
        String searchTerm = "testSearchTerm";
        String sortField = "testSortField";
        String sortOrder = "testSortOrder";
        Boolean onlyShowSentNotifications = Boolean.TRUE;

        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);

        Mockito.when(auditEntryRepository.findMatchingAudit(Mockito.anyLong(), Mockito.any(UUID.class))).thenReturn(Optional.empty());
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, null, notificationManager, null);

        PageRequest pageRequest = Mockito.mock(PageRequest.class);
        Mockito.when(notificationManager.getPageRequestForNotifications(pageNumber, pageSize, sortField, sortOrder)).thenReturn(pageRequest);
        String content = "contentString";

        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(1L, 1L, "provider", "providerConfigName", "notificationType", content, new Date(), new Date());
        Pageable auditPageable = Mockito.mock(Pageable.class);
        Mockito.when(auditPageable.getOffset()).thenReturn(pageNumber.longValue());
        Mockito.when(auditPageable.getPageSize()).thenReturn(pageSize);
        Page<AlertNotificationModel> auditPage = new PageImpl<>(List.of(alertNotificationModel), auditPageable, 1);
        //We cannot change the correct pageable because it is overriden by the default method.
        Page<AlertNotificationModel> spyAuditPage = Mockito.spy(auditPage);
        Mockito.doReturn(pageNumber).when(spyAuditPage).getNumber();
        Mockito.when(notificationManager.findAllWithSearch(searchTerm, pageRequest, onlyShowSentNotifications)).thenReturn(spyAuditPage);

        NotificationConfig notificationConfig = new NotificationConfig("3", "createdAtString", "providerString", 2L, "providerConfigNameString", "providerCreationTimeString", "notificationTypeString", content);
        String overallStatus = "overallStatusString";
        String lastSent = "lastSentString";
        AuditEntryModel auditEntryModel = new AuditEntryModel("2", notificationConfig, List.of(), overallStatus, lastSent);
        Function<AlertNotificationModel, AuditEntryModel> notificationToAuditEntryConverter = (AlertNotificationModel notificationModel) -> auditEntryModel;

        AlertPagedModel<AuditEntryModel> alertPagedModel = auditUtility.getPageOfAuditEntries(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications, notificationToAuditEntryConverter);

        assertEquals(1, alertPagedModel.getTotalPages());
        assertEquals(pageNumber.intValue(), alertPagedModel.getCurrentPage());
        assertEquals(pageSize, alertPagedModel.getPageSize());
        assertEquals(1, alertPagedModel.getContent().size());

        AuditEntryModel auditContentTest = alertPagedModel.getContent().stream().findFirst().orElse(null);

        assertEquals(auditEntryModel.getId(), auditContentTest.getId());
        assertEquals(notificationConfig, auditContentTest.getNotification());
        assertEquals(0, auditContentTest.getJobs().size());
        assertEquals(overallStatus, auditContentTest.getOverallStatus());
        assertEquals(lastSent, auditContentTest.getLastSent());
    }

    //TODO: We should take a look at the tests below and see if we want to replace them with new tests.

    //TODO: Remove this test later
    @Test
    public void createAuditEntryTest() throws Exception {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, null, null, null);
        ProviderMessageContent content = createMessageContent();
        UUID commonConfigUUID = UUID.randomUUID();
        AuditEntryEntity savedAuditEntryEntity = new AuditEntryEntity(commonConfigUUID, new Date(), new Date(), AuditEntryStatus.SUCCESS.toString(), null, null);
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
        Mockito.verify(auditNotificationRepository, Mockito.times(2)).save(Mockito.any(AuditNotificationRelation.class));
    }

    //TODO: Remove this test later
    @Test
    public void createAuditEntryNullEntryIdTest() throws Exception {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, null, null, null);
        ProviderMessageContent content = createMessageContent();
        UUID commonConfigUUID = UUID.randomUUID();
        AuditEntryEntity savedAuditEntryEntity = new AuditEntryEntity(commonConfigUUID, new Date(), new Date(), AuditEntryStatus.SUCCESS.toString(), null, null);
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

    //TODO: Remove this test later
    @Test
    public void setAuditEntrySuccessCatchExceptionTest() {
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(null, null, null, null, null);
        auditUtility.setAuditEntrySuccess(Collections.singletonList(1L));
    }

    //TODO: Remove this test later
    @Test
    public void setAuditEntrySuccessTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, null);

        AuditEntryEntity entity = new AuditEntryEntity(UUID.randomUUID(), new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS.toString(), null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntrySuccess(Collections.emptyList());
        auditUtility.setAuditEntrySuccess(Collections.singletonList(entity.getId()));
        assertEquals(AuditEntryStatus.SUCCESS.toString(), entity.getStatus());
    }

    //TODO: Remove this test later
    @Test
    public void setAuditEntryFailureCatchExceptionTest() {
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(null, null, null, null, null);
        auditUtility.setAuditEntryFailure(Collections.singletonList(1L), null, null);
    }

    //TODO: Remove this test later
    @Test
    public void setAuditEntryFailureTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, null);
        AuditEntryEntity entity = new AuditEntryEntity(UUID.randomUUID(), new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), AuditEntryStatus.FAILURE.toString(), null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntryFailure(Collections.emptyList(), null, null);
        auditUtility.setAuditEntryFailure(Collections.singletonList(entity.getId()), "error", new Exception());
        assertEquals(AuditEntryStatus.FAILURE.toString(), entity.getStatus());
        assertEquals("error", entity.getErrorMessage());
    }

    //TODO:  (May want to keep the helper method)
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
                   .applyTopic("Topic", "audit utility test", "https://google.com")
                   .applySubTopic(subTopic.getName(), subTopic.getValue(), subTopic.getUrl().orElse(null))
                   .applyAllComponentItems(List.of(componentItem_1, componentItem_2, componentItem_3))
                   .build();
    }

    //TODO:  (May want to keep the helper method)
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
