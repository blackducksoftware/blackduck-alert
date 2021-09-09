/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public class EmailJobDetailsModel extends DistributionJobDetailsModel {
    private final Properties javamailProperties;
    private final String smtpFrom;
    private final String smtpHost;
    private final int smtpPort;
    private final boolean smtpAuth;
    private final String smtpUsername;
    private final String smtpPassword;
    private final @Nullable String subjectLine;
    private final boolean projectOwnerOnly;
    private final boolean additionalEmailAddressesOnly;
    // TODO use enum: EmailAttachmentFormat
    private final String attachmentFileType;
    private final List<String> additionalEmailAddresses;

    public EmailJobDetailsModel(UUID jobId, Properties javamailProperties, String smtpFrom, String smtpHost, int smtpPort, boolean smtpAuth, String smtpUsername, String smtpPassword, @Nullable String subjectLine, boolean projectOwnerOnly, boolean additionalEmailAddressesOnly, String attachmentFileType, List<String> additionalEmailAddresses) {
        super(ChannelKeys.EMAIL, jobId);
        this.javamailProperties = javamailProperties;
        this.smtpFrom = smtpFrom;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpAuth = smtpAuth;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
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

    public Properties getJavamailProperties() {
        return javamailProperties;
    }

    public String getSmtpFrom() {
        return smtpFrom;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }
}
