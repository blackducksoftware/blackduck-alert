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
package com.synopsys.integration.alert.database.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.database.entity.NotificationContent;

public interface NotificationContentRepository extends JpaRepository<NotificationContent, Long> {
    //    @Query("SELECT entity FROM NotificationContent entity WHERE entity.createdAt BETWEEN ?1 AND ?2 ORDER BY created_at, provider_creation_time asc")
    @Query(value = "SELECT entity FROM NotificationContent entity")
    List<NotificationContent> findByCreatedAtBetween(final Date startDate, final Date endDate);

    //    @Query("SELECT entity FROM NotificationContent entity WHERE entity.createdAt < ?1 ORDER BY created_at, provider_creation_time asc")
    @Query(value = "SELECT entity FROM NotificationContent entity")
    List<NotificationContent> findByCreatedAtBefore(final Date date);

    //    @Query(value = "SELECT entity FROM NotificationContent entity WHERE entity.id IN (SELECT notificationId FROM entity.auditNotificationRelations WHERE entity.id = notificationId)")
    @Query(value = "SELECT entity FROM NotificationContent entity")
    Page<NotificationContent> findAllSentNotifications(final Pageable pageable);

    @Query(value = "SELECT entity FROM NotificationContent entity LEFT JOIN entity.auditNotificationRelations relation ON entity.id = relation.notificationId "
                       + "LEFT JOIN relation.auditEntryEntity auditEntry ON auditEntry.id = relation.auditEntryId "
                       + "LEFT JOIN auditEntry.descriptorConfigEntity descriptorConfig ON auditEntry.commonConfigId = descriptorConfig.id "
                       + "LEFT JOIN descriptorConfig.fieldValueEntities fieldValue ON descriptorConfig.id = fieldValue.configId "
                       + "LEFT JOIN fieldValue.definedFieldEntity definedField ON fieldValue.fieldId = definedField.id "
                       + "WHERE LOWER(entity.provider) LIKE %:searchTerm% OR "
                       + "LOWER(entity.notificationType) LIKE %:searchTerm% OR "
                       + "LOWER(entity.content) LIKE %:searchTerm% OR "
                       + "LOWER(entity.createdAt) LIKE %:searchTerm% OR "
                       + "LOWER(auditEntry.timeLastSent) LIKE %:searchTerm% OR "
                       + "LOWER(auditEntry.status) LIKE %:searchTerm% OR "
                       + "(definedField.key = '" + CommonDistributionUIConfig.KEY_NAME + "' AND LOWER(fieldValue.value) LIKE %:searchTerm% ) OR "
                       + "(definedField.key = '" + CommonDistributionUIConfig.KEY_CHANNEL_NAME + "' AND LOWER(fieldValue.value) LIKE %:searchTerm% )")
    Page<NotificationContent> findMatchingNotification(@Param("searchTerm") String searchTerm, final Pageable pageable);

    @Query(value = "SELECT entity FROM NotificationContent entity LEFT JOIN entity.auditNotificationRelations relation ON entity.id = relation.notificationId "
                       + "LEFT JOIN relation.auditEntryEntity auditEntry ON auditEntry.id = relation.auditEntryId "
                       + "LEFT JOIN auditEntry.descriptorConfigEntity descriptorConfig ON auditEntry.commonConfigId = descriptorConfig.id "
                       + "LEFT JOIN descriptorConfig.fieldValueEntities fieldValue ON descriptorConfig.id = fieldValue.configId "
                       + "LEFT JOIN fieldValue.definedFieldEntity definedField ON fieldValue.fieldId = definedField.id "
                       + "WHERE entity.id IN (SELECT notificationId FROM entity.auditNotificationRelations WHERE entity.id = notificationId) AND "
                       + "("
                       + "LOWER(entity.provider) LIKE %:searchTerm% OR "
                       + "LOWER(entity.notificationType) LIKE %:searchTerm% OR "
                       + "LOWER(entity.content) LIKE %:searchTerm% OR "
                       + "LOWER(entity.createdAt) LIKE %:searchTerm% OR "
                       + "LOWER(auditEntry.timeLastSent) LIKE %:searchTerm% OR "
                       + "LOWER(auditEntry.status) LIKE %:searchTerm% OR "
                       + "(definedField.key = '" + CommonDistributionUIConfig.KEY_NAME + "' AND LOWER(fieldValue.value) LIKE %:searchTerm% ) OR "
                       + "(definedField.key = '" + CommonDistributionUIConfig.KEY_CHANNEL_NAME + "' AND LOWER(fieldValue.value) LIKE %:searchTerm% )"
                       + ")")
    Page<NotificationContent> findMatchingSentNotification(@Param("searchTerm") String searchTerm, final Pageable pageable);

}
