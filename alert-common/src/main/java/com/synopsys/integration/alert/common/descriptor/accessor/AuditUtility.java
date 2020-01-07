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

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface AuditUtility {
    Optional<Long> findMatchingAuditId(final Long notificationId, final UUID commonDistributionId);

    Optional<AuditJobStatusModel> findFirstByJobId(final UUID jobId);

    AlertPagedModel<AuditEntryModel> getPageOfAuditEntries(final Integer pageNumber, final Integer pageSize, final String searchTerm, final String sortField, final String sortOrder, final boolean onlyShowSentNotifications,
        final Function<AlertNotificationWrapper, AuditEntryModel> notificationToAuditEntryConverter);

    Map<Long, Long> createAuditEntry(final Map<Long, Long> existingNotificationIdToAuditId, final UUID jobId, final MessageContentGroup content);

    void setAuditEntrySuccess(final Collection<Long> auditEntryIds);

    void setAuditEntryFailure(final Collection<Long> auditEntryIds, final String errorMessage, final Throwable t);

    AuditEntryModel convertToAuditEntryModelFromNotification(final AlertNotificationWrapper notificationContentEntry);

    AuditEntryStatus getWorstStatus(final AuditEntryStatus overallStatus, final AuditEntryStatus currentStatus);
}
