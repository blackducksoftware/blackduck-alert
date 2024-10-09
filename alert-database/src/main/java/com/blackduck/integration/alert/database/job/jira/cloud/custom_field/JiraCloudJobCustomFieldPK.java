/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.jira.cloud.custom_field;

import java.io.Serializable;
import java.util.UUID;

public class JiraCloudJobCustomFieldPK implements Serializable {
    private UUID jobId;
    private String fieldName;

    public JiraCloudJobCustomFieldPK() {
    }

    public JiraCloudJobCustomFieldPK(UUID jobId, String fieldName) {
        this.jobId = jobId;
        this.fieldName = fieldName;
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getFieldName() {
        return fieldName;
    }

}
