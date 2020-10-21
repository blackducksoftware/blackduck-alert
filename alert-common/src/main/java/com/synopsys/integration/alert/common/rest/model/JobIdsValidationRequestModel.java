package com.synopsys.integration.alert.common.rest.model;

import java.util.List;
import java.util.UUID;

public class JobIdsValidationRequestModel extends AlertSerializableModel {
    private List<UUID> jobIds;

    public JobIdsValidationRequestModel() {
        // For serialization
    }

    public JobIdsValidationRequestModel(List<UUID> jobIds) {
        this.jobIds = jobIds;
    }

    public List<UUID> getJobIds() {
        return jobIds;
    }

}
