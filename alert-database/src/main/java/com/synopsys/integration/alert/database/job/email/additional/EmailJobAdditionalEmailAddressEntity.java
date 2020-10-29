/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
