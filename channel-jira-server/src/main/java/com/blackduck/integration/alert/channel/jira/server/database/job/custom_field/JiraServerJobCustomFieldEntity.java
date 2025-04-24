/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.database.job.custom_field;

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

    @Column(name = "treat_value_as_json")
    private boolean treatValueAsJson;

    public JiraServerJobCustomFieldEntity() {
    }

    public JiraServerJobCustomFieldEntity(UUID jobId, String fieldName, String fieldValue, boolean treatValueAsJson) {
        this.jobId = jobId;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.treatValueAsJson = treatValueAsJson;
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

    public boolean isTreatValueAsJson() {
        return treatValueAsJson;
    }

}
