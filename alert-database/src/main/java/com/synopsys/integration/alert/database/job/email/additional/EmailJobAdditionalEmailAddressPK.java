package com.synopsys.integration.alert.database.job.email.additional;

import java.io.Serializable;
import java.util.UUID;

public class EmailJobAdditionalEmailAddressPK implements Serializable {
    private UUID jobId;
    private String emailAddress;

    public EmailJobAdditionalEmailAddressPK() {
    }

    public EmailJobAdditionalEmailAddressPK(UUID jobId, String emailAddress) {
        this.jobId = jobId;
        this.emailAddress = emailAddress;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

}
