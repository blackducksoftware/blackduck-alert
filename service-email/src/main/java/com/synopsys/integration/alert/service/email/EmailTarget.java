/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmailTarget {
    private final Set<String> emailAddresses;
    private final String templateName;
    private final Map<String, Object> model;
    private final Map<String, String> contentIdsToFilePaths;
    private List<String> attachmentFilePaths;

    public EmailTarget(Set<String> emailAddresses, String templateName, Map<String, Object> model, Map<String, String> contentIdsToFilePaths) {
        this.emailAddresses = emailAddresses;
        this.templateName = templateName;
        this.model = model;
        this.contentIdsToFilePaths = contentIdsToFilePaths;
        this.attachmentFilePaths = List.of();
    }

    public EmailTarget(String emailAddress, String templateName, Map<String, Object> model, Map<String, String> contentIdsToFilePaths) {
        this(Set.of(emailAddress), templateName, model, contentIdsToFilePaths);
    }

    public Set<String> getEmailAddresses() {
        return emailAddresses;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public Map<String, String> getContentIdsToFilePaths() {
        return contentIdsToFilePaths;
    }

    public List<String> getAttachmentFilePaths() {
        return attachmentFilePaths;
    }

    public void setAttachmentFilePath(String attachmentFilePath) {
        if (null != attachmentFilePath) {
            setAttachmentFilePaths(List.of(attachmentFilePath));
        }
    }

    public void setAttachmentFilePaths(List<String> attachmentFilePaths) {
        if (null != attachmentFilePaths) {
            this.attachmentFilePaths = attachmentFilePaths;
        }
    }

}
