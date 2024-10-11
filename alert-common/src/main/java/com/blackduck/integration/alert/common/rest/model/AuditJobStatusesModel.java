/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.model;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;

public class AuditJobStatusesModel extends AlertSerializableModel {
    private final List<AuditJobStatusModel> statuses;

    public AuditJobStatusesModel() {
        // For serialization
        this.statuses = List.of();
    }

    public AuditJobStatusesModel(List<AuditJobStatusModel> statuses) {
        this.statuses = statuses;
    }

    public List<AuditJobStatusModel> getStatuses() {
        return statuses;
    }

}
