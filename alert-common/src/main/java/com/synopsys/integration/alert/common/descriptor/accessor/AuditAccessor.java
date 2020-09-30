/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.accessor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;

public interface AuditAccessor {
    Optional<Long> findMatchingAuditId(Long notificationId, UUID commonDistributionId);

    Optional<AuditJobStatusModel> findFirstByJobId(UUID jobId);

    AuditEntryPageModel getPageOfAuditEntries(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder, boolean onlyShowSentNotifications,
        Function<AlertNotificationModel, AuditEntryModel> notificationToAuditEntryConverter);

    Map<Long, Long> createAuditEntry(Map<Long, Long> existingNotificationIdToAuditId, UUID jobId, MessageContentGroup content);

    void setAuditEntrySuccess(Collection<Long> auditEntryIds);

    void setAuditEntryFailure(Collection<Long> auditEntryIds, String errorMessage, Throwable t);

    AuditEntryModel convertToAuditEntryModelFromNotification(AlertNotificationModel notificationContentEntry);
}
