/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.model;

import java.util.List;
import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

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
