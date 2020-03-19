/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AuditNotificationRepository extends JpaRepository<AuditNotificationRelation, AuditNotificationRelationPK> {
    List<AuditNotificationRelation> findByAuditEntryId(Long auditEntryId);

    List<AuditNotificationRelation> findByNotificationId(Long notificationId);

    // 03-19-2020 psantos:
    // Had to write the query for deletion manually. JPA will throw an exception if the method is declared only to derive the delete query via JPA.
    // Delete by the notification id. In case the audit entry id is deleted we need to delete any relations with with the notificationId. Doesn't occur in newer versions.
    @Modifying
    @Query("DELETE FROM AuditNotificationRelation relation WHERE relation.notificationId = ?1")
    void deleteByNotificationId(Long notificationId);
}
