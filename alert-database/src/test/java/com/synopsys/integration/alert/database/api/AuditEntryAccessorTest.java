package com.synopsys.integration.alert.database.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;

public class AuditEntryAccessorTest {

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

    @Test
    public void setAuditEntrySuccessCatchExceptionTest() {
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(null, null, null, null, null);
        auditUtility.setAuditEntrySuccess(Collections.singletonList(1L));
    }

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

    @Test
    public void setAuditEntryFailureCatchExceptionTest() {
        DefaultAuditUtility auditUtility = new DefaultAuditUtility(null, null, null, null, null);
        auditUtility.setAuditEntryFailure(Collections.singletonList(1L), null, null);
    }

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
