/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.message.model;

import java.time.OffsetDateTime;

import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

public class CommonMessageData {
    private final Long notificationId;
    private final Long providerConfigId;
    private final String providerName;
    private final String providerConfigName;
    private final String providerURL;
    private final OffsetDateTime providerCreationDate;
    private final DistributionJobModel job;

    public CommonMessageData(Long notificationId, Long providerConfigId, String providerName, String providerConfigName, String providerURL, OffsetDateTime providerCreationDate, DistributionJobModel job) {
        this.notificationId = notificationId;
        this.providerConfigId = providerConfigId;
        this.providerName = providerName;
        this.providerConfigName = providerConfigName;
        this.providerURL = providerURL;
        this.providerCreationDate = providerCreationDate;
        this.job = job;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProviderConfigName() {
        return providerConfigName;
    }

    public String getProviderURL() {
        return providerURL;
    }

    public OffsetDateTime getProviderCreationDate() {
        return providerCreationDate;
    }

    public DistributionJobModel getJob() {
        return job;
    }

}
