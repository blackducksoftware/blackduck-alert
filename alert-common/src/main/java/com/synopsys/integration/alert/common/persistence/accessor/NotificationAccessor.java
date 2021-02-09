/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.persistence.accessor;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;

public interface NotificationAccessor {
    List<AlertNotificationModel> saveAllNotifications(Collection<AlertNotificationModel> notifications);

    List<AlertNotificationModel> findByIds(List<Long> notificationIds);

    Optional<AlertNotificationModel> findById(Long notificationId);

    List<AlertNotificationModel> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate);

    List<AlertNotificationModel> findByCreatedAtBefore(OffsetDateTime date);

    List<AlertNotificationModel> findByCreatedAtBeforeDayOffset(int dayOffset);

    void deleteNotificationList(List<AlertNotificationModel> notifications);

    void deleteNotification(AlertNotificationModel notification);

}
