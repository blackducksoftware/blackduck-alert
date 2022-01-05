/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class JobIdsRequestModel extends AlertSerializableModel {
    private List<UUID> jobIds;

    public JobIdsRequestModel() {
        // For serialization
    }

    public JobIdsRequestModel(List<UUID> jobIds) {
        this.jobIds = jobIds;
    }

    public List<UUID> getJobIds() {
        return jobIds;
    }

}
