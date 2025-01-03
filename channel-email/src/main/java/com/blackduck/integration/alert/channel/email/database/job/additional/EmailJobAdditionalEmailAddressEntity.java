/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.database.job.additional;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

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
