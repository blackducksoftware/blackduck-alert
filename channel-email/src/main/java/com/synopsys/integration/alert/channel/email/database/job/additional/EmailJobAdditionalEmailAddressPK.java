/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.database.job.additional;

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
