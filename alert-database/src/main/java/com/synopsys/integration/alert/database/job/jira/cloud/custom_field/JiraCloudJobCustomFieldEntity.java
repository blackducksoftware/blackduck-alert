/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.jira.cloud.custom_field;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(JiraCloudJobCustomFieldPK.class)
@Table(schema = "alert", name = "jira_cloud_job_custom_fields")
public class JiraCloudJobCustomFieldEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Id
    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "field_value")
    private String fieldValue;

    public JiraCloudJobCustomFieldEntity() {
    }

    public JiraCloudJobCustomFieldEntity(UUID jobId, String fieldName, String fieldValue) {
        this.jobId = jobId;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

}
