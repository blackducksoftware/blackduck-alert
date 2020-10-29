package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.List;

public class EmailJobDetailsModel extends DistributionJobDetailsModel {
    private final String subjectLine;
    private final boolean projectOwnerOnly;
    private final boolean additionalEmailAddressesOnly;
    private final String attachmentFileType;
    private final List<String> additionalEmailAddresses;

    public EmailJobDetailsModel(String subjectLine, boolean projectOwnerOnly, boolean additionalEmailAddressesOnly, String attachmentFileType, List<String> additionalEmailAddresses) {
        super("channel_email");
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
