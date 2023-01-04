package com.synopsys.integration.alert.database.api.mock;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRelationPK;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAuditFailedNotificationRepository extends MockRepositoryContainer<AuditFailedNotificationRelationPK, AuditFailedNotificationRelation> implements
    AuditFailedNotificationRepository {

    public static AuditFailedNotificationRelationPK generateRelationKey(AuditFailedNotificationRelation relation) {
        AuditFailedNotificationRelationPK key = new AuditFailedNotificationRelationPK();
        key.setFailedAuditEntryId(relation.getFailedAuditEntryId());
        key.setNotificationId(relation.getNotificationId());
        return key;
    }

    public MockAuditFailedNotificationRepository(final Function<AuditFailedNotificationRelation, AuditFailedNotificationRelationPK> idGenerator) {
        super(idGenerator);
    }

    @Override
    public List<AuditFailedNotificationRelation> findAuditFailedNotificationRelationsByNotificationId(final Long notificationId) {
        Predicate<AuditFailedNotificationRelation> notificationIdEqual = relation -> relation.getNotificationId().equals(notificationId);
        return findAll().stream()
            .filter(notificationIdEqual)
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditFailedNotificationRelation> findAuditFailedNotificationRelationsByFailedAuditEntryId(final UUID failedAuditEntryId) {
        Predicate<AuditFailedNotificationRelation> notificationIdEqual = relation -> relation.getFailedAuditEntryId().equals(failedAuditEntryId);
        return findAll().stream()
            .filter(notificationIdEqual)
            .collect(Collectors.toList());
    }
}
