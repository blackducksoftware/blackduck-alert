/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job.details;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;

public class EmailJobDetailsModel extends DistributionJobDetailsModel {
    private final @Nullable String subjectLine;
    private final boolean projectOwnerOnly;
    private final boolean additionalEmailAddressesOnly;
    // TODO use enum: EmailAttachmentFormat
    private final String attachmentFileType;
    private final List<String> additionalEmailAddresses;

    public EmailJobDetailsModel(UUID jobId, @Nullable String subjectLine, boolean projectOwnerOnly, boolean additionalEmailAddressesOnly, String attachmentFileType, List<String> additionalEmailAddresses) {
        super(ChannelKeys.EMAIL, jobId);
        this.subjectLine = StringUtils.trimToNull(subjectLine);
        this.projectOwnerOnly = projectOwnerOnly;
        this.additionalEmailAddressesOnly = additionalEmailAddressesOnly;
        this.attachmentFileType = attachmentFileType;
        this.additionalEmailAddresses = additionalEmailAddresses;
    }

    public Optional<String> getSubjectLine() {
        return Optional.ofNullable(subjectLine);
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
