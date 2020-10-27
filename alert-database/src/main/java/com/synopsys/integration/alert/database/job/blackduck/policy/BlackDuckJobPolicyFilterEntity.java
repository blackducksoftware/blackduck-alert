package com.synopsys.integration.alert.database.job.blackduck.policy;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;

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

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private BlackDuckJobDetailsEntity blackDuckJobDetails;

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

    public BlackDuckJobDetailsEntity getBlackDuckJobDetails() {
        return blackDuckJobDetails;
    }

    public void setBlackDuckJobDetails(BlackDuckJobDetailsEntity blackDuckJobDetails) {
        this.blackDuckJobDetails = blackDuckJobDetails;
    }

}
