/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.blackduck.policy;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

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
