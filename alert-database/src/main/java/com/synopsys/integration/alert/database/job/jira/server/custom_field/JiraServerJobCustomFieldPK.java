/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.jira.server.custom_field;

import java.io.Serializable;
import java.util.UUID;

public class JiraServerJobCustomFieldPK implements Serializable {
    private UUID jobId;
    private String fieldName;

    public JiraServerJobCustomFieldPK() {
    }

    public JiraServerJobCustomFieldPK(UUID jobId, String fieldName) {
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
