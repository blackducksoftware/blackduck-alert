/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.blackduck.policy;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(BlackDuckJobPolicyFilterPK.class)
@Table(schema = "alert", name = "blackduck_job_policy_filters")
public class BlackDuckJobPolicyFilterEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Id
    @Column(name = "policy_name")
    private String policyName;

    public BlackDuckJobPolicyFilterEntity() {
    }

    public BlackDuckJobPolicyFilterEntity(UUID jobId, String policyName) {
        this.jobId = jobId;
        this.policyName = policyName;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

}
