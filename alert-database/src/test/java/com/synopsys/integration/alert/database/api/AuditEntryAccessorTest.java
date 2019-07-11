package com.synopsys.integration.alert.database.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;

public class AuditEntryAccessorTest {

    @Test
    public void createAuditEntryTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, null, null, null);
        final AggregateMessageContent content = createMessageContent();
        final UUID commonConfigUUID = UUID.randomUUID();
        final AuditEntryEntity savedAuditEntryEntity = new AuditEntryEntity(commonConfigUUID, new Date(), new Date(), AuditEntryStatus.SUCCESS.toString(), null, null);
        final Long auditID = 10L;
        savedAuditEntryEntity.setId(auditID);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(savedAuditEntryEntity));
        mockAuditRepositorySave(auditEntryRepository, savedAuditEntryEntity);

        final Map<Long, Long> existingNotificationIdToAuditId = new HashMap<>();
        existingNotificationIdToAuditId.put(1L, auditID);
        final Map<Long, Long> savedNotificationIdToAuditId = auditUtility.createAuditEntry(existingNotificationIdToAuditId, commonConfigUUID, MessageContentGroup.singleton(content));
        assertFalse(savedNotificationIdToAuditId.isEmpty());
        assertEquals(2, savedNotificationIdToAuditId.size());
        assertEquals(savedAuditEntryEntity.getId(), savedNotificationIdToAuditId.get(1L));
        assertEquals(AuditEntryStatus.PENDING.toString(), savedAuditEntryEntity.getStatus());
        Mockito.verify(auditEntryRepository).findById(Mockito.anyLong());
        Mockito.verify(auditNotificationRepository, Mockito.times(2)).save(Mockito.any(AuditNotificationRelation.class));
    }

    @Test
    public void createAuditEntryNullEntryIdTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, null, null, null);
        final AggregateMessageContent content = createMessageContent();
        final UUID commonConfigUUID = UUID.randomUUID();
        final AuditEntryEntity savedAuditEntryEntity = new AuditEntryEntity(commonConfigUUID, new Date(), new Date(), AuditEntryStatus.SUCCESS.toString(), null, null);
        savedAuditEntryEntity.setId(10L);

        mockAuditRepositorySave(auditEntryRepository, savedAuditEntryEntity);
        final Map<Long, Long> savedNotificationIdToAuditId = auditUtility.createAuditEntry(null, commonConfigUUID, MessageContentGroup.singleton(content));
        assertFalse(savedNotificationIdToAuditId.isEmpty());
        assertEquals(2, savedNotificationIdToAuditId.size());
        assertEquals(savedAuditEntryEntity.getId(), savedNotificationIdToAuditId.values().iterator().next());
        assertEquals(AuditEntryStatus.PENDING.toString(), savedAuditEntryEntity.getStatus());
        Mockito.verify(auditEntryRepository, Mockito.times(0)).findById(Mockito.anyLong());
        Mockito.verify(auditNotificationRepository, Mockito.times(2)).save(Mockito.any(AuditNotificationRelation.class));
    }

    @Test
    public void setAuditEntrySuccessCatchExceptionTest() {
        final DefaultAuditUtility auditUtility = new DefaultAuditUtility(null, null, null, null, null);
        auditUtility.setAuditEntrySuccess(Collections.singletonList(1L));
    }

    @Test
    public void setAuditEntrySuccessTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, null);

        final AuditEntryEntity entity = new AuditEntryEntity(UUID.randomUUID(), new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS.toString(), null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntrySuccess(Collections.emptyList());
        auditUtility.setAuditEntrySuccess(Collections.singletonList(entity.getId()));
        assertEquals(AuditEntryStatus.SUCCESS.toString(), entity.getStatus());
    }

    @Test
    public void setAuditEntryFailureCatchExceptionTest() {
        final DefaultAuditUtility auditUtility = new DefaultAuditUtility(null, null, null, null, null);
        auditUtility.setAuditEntryFailure(Collections.singletonList(1L), null, null);
    }

    @Test
    public void setAuditEntryFailureTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final DefaultAuditUtility auditUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, null);
        final AuditEntryEntity entity = new AuditEntryEntity(UUID.randomUUID(), new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), AuditEntryStatus.FAILURE.toString(), null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntryFailure(Collections.emptyList(), null, null);
        auditUtility.setAuditEntryFailure(Collections.singletonList(entity.getId()), "error", new Exception());
        assertEquals(AuditEntryStatus.FAILURE.toString(), entity.getStatus());
        assertEquals("error", entity.getErrorMessage());
    }

    public AggregateMessageContent createMessageContent() {
        final LinkableItem linkableItem1 = new LinkableItem("First Linkable Item", "Value 1", "https://google.com");
        final LinkableItem linkableItem2 = new LinkableItem("Second Linkable Item", "Value 2", "https://google.com");

        final String nameKey = "Same Key";
        final LinkableItem linkableItem3 = new LinkableItem(nameKey, "Value", "https://google.com");
        final LinkableItem linkableItem4 = new LinkableItem(nameKey, "No Link Value");
        final LinkableItem linkableItem5 = new LinkableItem(nameKey, "Other Value", "https://google.com");

        final CategoryItem categoryItem1 = new CategoryItem(CategoryKey.from("TYPE", "data1", "data2"), ItemOperation.ADD, 1L, new TreeSet<>(Arrays.asList(linkableItem1, linkableItem2)));
        final CategoryItem categoryItem2 = new CategoryItem(CategoryKey.from("TYPE", "data1", "data2"), ItemOperation.UPDATE, 2L, new TreeSet<>(Collections.singletonList(linkableItem2)));
        final CategoryItem categoryItem3 = new CategoryItem(CategoryKey.from("TYPE", "data1", "data2"), ItemOperation.DELETE, 1L, new TreeSet<>(Arrays.asList(linkableItem3, linkableItem4, linkableItem5)));
        final LinkableItem subTopic = new LinkableItem("Sub Topic", "Sub Topic Value", "https://google.com");

        final SortedSet<CategoryItem> categoryItems = new TreeSet<>();
        categoryItems.add(categoryItem1);
        categoryItems.add(categoryItem2);
        categoryItems.add(categoryItem3);

        return new AggregateMessageContent("Topic", "audit utility test", "https://google.com", subTopic, categoryItems, Date.from(Instant.now()));
    }

    private void mockAuditRepositorySave(final AuditEntryRepository auditEntryRepository, final AuditEntryEntity savedAuditEntryEntity) {
        Mockito.when(auditEntryRepository.save(Mockito.any(AuditEntryEntity.class))).then(invocation -> {
            final AuditEntryEntity originalEntity = invocation.getArgument(0);
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
