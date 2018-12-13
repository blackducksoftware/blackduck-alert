/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database.audit;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditEntryRepository extends JpaRepository<AuditEntryEntity, Long> {
    AuditEntryEntity findFirstByCommonConfigIdOrderByTimeLastSentDesc(final Long commonConfigId);

    List<AuditEntryEntity> findByCommonConfigId(final Long commonConfigId);

    //    @Query(value = "SELECT entity FROM AuditEntryEntity entity JOIN entity.auditNotificationRelations relation ON entity.id = relation.auditEntryId WHERE entity.commonConfigId = ?2 AND relation.notificationContent.id = ?1")
    @Query(value = "SELECT entity FROM AuditEntryEntity entity")
    Optional<AuditEntryEntity> findMatchingAudit(Long notificationId, Long commonConfigId);

}
