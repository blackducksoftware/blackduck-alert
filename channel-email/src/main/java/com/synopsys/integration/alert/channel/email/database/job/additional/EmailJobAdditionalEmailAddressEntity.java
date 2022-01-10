/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.database.job.additional;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(EmailJobAdditionalEmailAddressPK.class)
@Table(schema = "alert", name = "email_job_additional_email_addresses")
public class EmailJobAdditionalEmailAddressEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "email_address")
    private String emailAddress;

    public EmailJobAdditionalEmailAddressEntity() {
    }

    public EmailJobAdditionalEmailAddressEntity(UUID jobId, String emailAddress) {
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
