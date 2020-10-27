package com.synopsys.integration.alert.database.job.email.additional;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.job.email.EmailJobDetailsEntity;

@Entity
@IdClass(EmailJobAdditionalEmailAddressPK.class)
@Table(schema = "alert", name = "email_job_additional_email_addresses")
public class EmailJobAdditionalEmailAddressEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "email_address")
    private String emailAddress;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private EmailJobDetailsEntity emailJobDetails;

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

    public EmailJobDetailsEntity getEmailJobDetails() {
        return emailJobDetails;
    }

    public void setEmailJobDetails(EmailJobDetailsEntity emailJobDetails) {
        this.emailJobDetails = emailJobDetails;
    }

}
