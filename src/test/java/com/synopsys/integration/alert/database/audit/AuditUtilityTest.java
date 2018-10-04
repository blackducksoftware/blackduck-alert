package com.synopsys.integration.alert.database.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.CategoryKey;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;

public class AuditUtilityTest {

    @Test
    public void createAuditEntryTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final AuditUtility auditUtility = new AuditUtility(auditEntryRepository, auditNotificationRepository);
        final AggregateMessageContent content = createMessageContent();
        final AuditEntryEntity savedAuditEntryEntity = new AuditEntryEntity(1L, new Date(), new Date(), AuditEntryStatus.SUCCESS, null, null);
        savedAuditEntryEntity.setId(10L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(savedAuditEntryEntity));
        mockAuditRepositorySave(auditEntryRepository, savedAuditEntryEntity);

        final Long auditEntryId = auditUtility.createAuditEntry(10L, 1L, content);
        assertEquals(savedAuditEntryEntity.getId(), auditEntryId);
        assertEquals(AuditEntryStatus.PENDING, savedAuditEntryEntity.getStatus());
        Mockito.verify(auditEntryRepository).findById(Mockito.anyLong());
        Mockito.verify(auditNotificationRepository, Mockito.times(content.getCategoryItemList().size())).save(Mockito.any(AuditNotificationRelation.class));
    }

    @Test
    public void createAuditEntryNullEntryIdTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final AuditUtility auditUtility = new AuditUtility(auditEntryRepository, auditNotificationRepository);
        final AggregateMessageContent content = createMessageContent();
        final AuditEntryEntity savedAuditEntryEntity = new AuditEntryEntity(1L, new Date(), new Date(), AuditEntryStatus.SUCCESS, null, null);
        savedAuditEntryEntity.setId(10L);

        mockAuditRepositorySave(auditEntryRepository, savedAuditEntryEntity);
        final Long auditEntryId = auditUtility.createAuditEntry(null, 1L, content);
        assertNotNull(auditEntryId);
        assertEquals(savedAuditEntryEntity.getId(), auditEntryId);
        assertEquals(AuditEntryStatus.PENDING, savedAuditEntryEntity.getStatus());
        Mockito.verify(auditEntryRepository, Mockito.times(0)).findById(Mockito.anyLong());
        Mockito.verify(auditNotificationRepository, Mockito.times(content.getCategoryItemList().size())).save(Mockito.any(AuditNotificationRelation.class));
    }

    @Test
    public void setAuditEntrySuccessCatchExceptionTest() {
        final AuditUtility auditUtility = new AuditUtility(null, null);
        auditUtility.setAuditEntrySuccess(1L);
    }

    @Test
    public void setAuditEntrySuccessTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final AuditUtility auditUtility = new AuditUtility(auditEntryRepository, null);

        final AuditEntryEntity entity = new AuditEntryEntity(1L, new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS, null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntrySuccess(null);
        auditUtility.setAuditEntrySuccess(entity.getId());
        assertEquals(AuditEntryStatus.SUCCESS, entity.getStatus());
    }

    @Test
    public void setAuditEntryFailureCatchExceptionTest() {
        final AuditUtility auditUtility = new AuditUtility(null, null);
        auditUtility.setAuditEntryFailure(1L, null, null);
    }

    @Test
    public void setAuditEntryFailureTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final AuditUtility auditUtility = new AuditUtility(auditEntryRepository, null);
        final AuditEntryEntity entity = new AuditEntryEntity(1L, new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), AuditEntryStatus.FAILURE, null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntryFailure(null, null, null);
        auditUtility.setAuditEntryFailure(entity.getId(), "error", new Exception());
        assertEquals(AuditEntryStatus.FAILURE, entity.getStatus());
        assertEquals("error", entity.getErrorMessage());
    }

    public AggregateMessageContent createMessageContent() {
        final LinkableItem linkableItem1 = new LinkableItem("First Linkable Item", "Value 1", "https://google.com");
        final LinkableItem linkableItem2 = new LinkableItem("Second Linkable Item", "Value 2", "https://google.com");

        final String nameKey = "Same Key";
        final LinkableItem linkableItem3 = new LinkableItem(nameKey, "Value", "https://google.com");
        final LinkableItem linkableItem4 = new LinkableItem(nameKey, "No Link Value");
        final LinkableItem linkableItem5 = new LinkableItem(nameKey, "Other Value", "https://google.com");

        final CategoryItem categoryItem1 = new CategoryItem(CategoryKey.from("TYPE", "data1", "data2"), ItemOperation.ADD, 1L, Arrays.asList(linkableItem1, linkableItem2));
        final CategoryItem categoryItem2 = new CategoryItem(CategoryKey.from("TYPE", "data1", "data2"), ItemOperation.UPDATE, 2L, Arrays.asList(linkableItem2));
        final CategoryItem categoryItem3 = new CategoryItem(CategoryKey.from("TYPE", "data1", "data2"), ItemOperation.DELETE, 1L, Arrays.asList(linkableItem3, linkableItem4, linkableItem5));
        final LinkableItem subTopic = new LinkableItem("Sub Topic", "Sub Topic Value", "https://google.com");
        return new AggregateMessageContent("Topic", "audit utility test", "https://google.com", subTopic, Arrays.asList(categoryItem1, categoryItem2, categoryItem3));
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
