/*
 * alert-common
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
package com.synopsys.integration.alert.common.email;

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
