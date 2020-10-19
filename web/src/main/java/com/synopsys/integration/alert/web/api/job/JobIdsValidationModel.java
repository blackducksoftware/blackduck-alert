package com.synopsys.integration.alert.web.api.job;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class JobIdsValidationModel extends AlertSerializableModel {
    private List<UUID> jobIds;

    public JobIdsValidationModel() {
        // For serialization
    }

    public JobIdsValidationModel(List<UUID> jobIds) {
        this.jobIds = jobIds;
    }

    public List<UUID> getJobIds() {
        return jobIds;
    }

}
