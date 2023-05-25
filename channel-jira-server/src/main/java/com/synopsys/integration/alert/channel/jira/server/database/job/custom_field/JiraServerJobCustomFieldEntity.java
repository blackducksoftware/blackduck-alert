/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.database.job.custom_field;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(JiraServerJobCustomFieldPK.class)
@Table(schema = "alert", name = "jira_server_job_custom_fields")
public class JiraServerJobCustomFieldEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Id
    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "field_value")
    private String fieldValue;

    public JiraServerJobCustomFieldEntity() {
    }

    public JiraServerJobCustomFieldEntity(UUID jobId, String fieldName, String fieldValue) {
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
