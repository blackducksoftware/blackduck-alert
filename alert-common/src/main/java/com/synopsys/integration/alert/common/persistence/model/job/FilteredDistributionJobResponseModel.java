/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.UUID;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class FilteredDistributionJobResponseModel extends AlertSerializableModel {
    private final UUID id;
    private final ProcessingType processingType;
    private final String channelName;

    public FilteredDistributionJobResponseModel(UUID id, ProcessingType processingType, String channelName) {
        this.processingType = processingType;
        this.id = id;
        this.channelName = channelName;
    }

    public UUID getId() {
        return id;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public String getChannelName() {
        return channelName;
    }

}
