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
package com.synopsys.integration.alert.common.event;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

public class DistributionEvent extends ContentEvent {
    private static final long serialVersionUID = -7858733753649257748L;

    private final DistributionJobModel distributionJobModel;
    private final ConfigurationModel channelGlobalConfig;
    private Map<Long, Long> notificationIdToAuditId;

    public DistributionEvent(
        String destination,
        String createdAt,
        Long providerConfigId,
        String formatType,
        MessageContentGroup contentGroup,
        DistributionJobModel distributionJobModel,
        @Nullable ConfigurationModel channelGlobalConfig
    ) {
        super(destination, createdAt, providerConfigId, formatType, contentGroup);
        this.distributionJobModel = distributionJobModel;
        this.channelGlobalConfig = channelGlobalConfig;
    }

    public DistributionJobModel getDistributionJobModel() {
        return distributionJobModel;
    }

    public Optional<ConfigurationModel> getChannelGlobalConfig() {
        return Optional.ofNullable(channelGlobalConfig);
    }

    public Map<Long, Long> getNotificationIdToAuditId() {
        return notificationIdToAuditId;
    }

    public void setNotificationIdToAuditId(Map<Long, Long> notificationIdToAuditId) {
        this.notificationIdToAuditId = notificationIdToAuditId;
    }

    public Set<Long> getAuditIds() {
        if (null != notificationIdToAuditId && !notificationIdToAuditId.isEmpty()) {
            return notificationIdToAuditId.entrySet()
                       .stream()
                       .map(Map.Entry::getValue)
                       .filter(Objects::nonNull)
                       .collect(Collectors.toSet());
        }
        return Set.of();
    }

}
