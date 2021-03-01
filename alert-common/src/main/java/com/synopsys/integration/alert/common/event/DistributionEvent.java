/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
