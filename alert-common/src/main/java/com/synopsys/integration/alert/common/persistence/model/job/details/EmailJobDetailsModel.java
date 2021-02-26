/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public class EmailJobDetailsModel extends DistributionJobDetailsModel {
    private final String subjectLine;
    private final boolean projectOwnerOnly;
    private final boolean additionalEmailAddressesOnly;
    private final String attachmentFileType;
    private final List<String> additionalEmailAddresses;

    public EmailJobDetailsModel(UUID jobId, String subjectLine, boolean projectOwnerOnly, boolean additionalEmailAddressesOnly, String attachmentFileType, List<String> additionalEmailAddresses) {
        super(ChannelKeys.EMAIL, jobId);
        this.subjectLine = subjectLine;
        this.projectOwnerOnly = projectOwnerOnly;
        this.additionalEmailAddressesOnly = additionalEmailAddressesOnly;
        this.attachmentFileType = attachmentFileType;
        this.additionalEmailAddresses = additionalEmailAddresses;
    }

    public String getSubjectLine() {
        return subjectLine;
    }

    public boolean isProjectOwnerOnly() {
        return projectOwnerOnly;
    }

    public boolean isAdditionalEmailAddressesOnly() {
        return additionalEmailAddressesOnly;
    }

    public String getAttachmentFileType() {
        return attachmentFileType;
    }

    public List<String> getAdditionalEmailAddresses() {
        return additionalEmailAddresses;
    }

}
