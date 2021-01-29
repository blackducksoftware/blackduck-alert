/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.database.job.email;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.job.email.additional.EmailJobAdditionalEmailAddressEntity;

@Entity
@Table(schema = "alert", name = "email_job_details")
public class EmailJobDetailsEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "subject_line")
    private String subjectLine;

    @Column(name = "project_owner_only")
    private Boolean projectOwnerOnly;

    @Column(name = "additional_email_addresses_only")
    private Boolean additionalEmailAddressesOnly;

    @Column(name = "attachment_file_type")
    private String attachmentFileType;

    @OneToMany
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private List<EmailJobAdditionalEmailAddressEntity> emailJobAdditionalEmailAddresses;

    public EmailJobDetailsEntity() {
    }

    public EmailJobDetailsEntity(UUID jobId, String subjectLine, Boolean projectOwnerOnly, Boolean additionalEmailAddressesOnly, String attachmentFileType) {
        this.jobId = jobId;
        this.subjectLine = subjectLine;
        this.projectOwnerOnly = projectOwnerOnly;
        this.additionalEmailAddressesOnly = additionalEmailAddressesOnly;
        this.attachmentFileType = attachmentFileType;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getSubjectLine() {
        return subjectLine;
    }

    public void setSubjectLine(String subjectLine) {
        this.subjectLine = subjectLine;
    }

    public Boolean getProjectOwnerOnly() {
        return projectOwnerOnly;
    }

    public void setProjectOwnerOnly(Boolean projectOwnerOnly) {
        this.projectOwnerOnly = projectOwnerOnly;
    }

    public Boolean getAdditionalEmailAddressesOnly() {
        return additionalEmailAddressesOnly;
    }

    public void setAdditionalEmailAddressesOnly(Boolean additionalEmailAddressesOnly) {
        this.additionalEmailAddressesOnly = additionalEmailAddressesOnly;
    }

    public String getAttachmentFileType() {
        return attachmentFileType;
    }

    public void setAttachmentFileType(String attachmentFileType) {
        this.attachmentFileType = attachmentFileType;
    }

    public List<EmailJobAdditionalEmailAddressEntity> getEmailJobAdditionalEmailAddresses() {
        return emailJobAdditionalEmailAddresses;
    }

    public void setEmailJobAdditionalEmailAddresses(List<EmailJobAdditionalEmailAddressEntity> emailJobAdditionalEmailAddresses) {
        this.emailJobAdditionalEmailAddresses = emailJobAdditionalEmailAddresses;
    }

}
