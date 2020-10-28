package com.synopsys.integration.alert.database.job.blackduck.policy;

import java.io.Serializable;
import java.util.UUID;

public class BlackDuckJobPolicyFilterPK implements Serializable {
    private UUID jobId;
    private String policyName;

    public BlackDuckJobPolicyFilterPK() {
    }

    public BlackDuckJobPolicyFilterPK(UUID jobId, String policyName) {
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
