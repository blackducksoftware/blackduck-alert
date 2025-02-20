/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.mock;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.blackduck.integration.alert.database.audit.AuditNotificationRelation;
import com.blackduck.integration.alert.database.audit.AuditNotificationRelationPK;
import com.blackduck.integration.alert.database.audit.AuditNotificationRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAuditNotificationRepository extends MockRepositoryContainer<AuditNotificationRelationPK, AuditNotificationRelation> implements AuditNotificationRepository {

    public MockAuditNotificationRepository(Function<AuditNotificationRelation, AuditNotificationRelationPK> idGenerator) {
        super(idGenerator);
    }

    @Override
    public List<AuditNotificationRelation> findByAuditEntryId(Long auditEntryId) {
        return getDataMap().values().stream()
            .filter(relation -> relation.getAuditEntryId().equals(auditEntryId))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditNotificationRelation> findByNotificationId(Long notificationId) {
        return getDataMap().values().stream()
            .filter(relation -> relation.getNotificationId().equals(notificationId))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditNotificationRelation> findAllByNotificationIdIn(List<Long> notificationIds) {
        return getDataMap().values().stream()
            .filter(relation -> notificationIds.contains(relation.getNotificationId()))
            .collect(Collectors.toList());
    }
}
