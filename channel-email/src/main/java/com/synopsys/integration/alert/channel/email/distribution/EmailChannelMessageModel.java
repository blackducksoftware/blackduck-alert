/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class EmailChannelMessageModel extends AlertSerializableModel {
    private final String subjectLine;
    private final String content;

    private final String messageFormat;

    private final String providerName;
    private final String providerUrl;
    private final String projectName;
    private final ProjectMessage source;

    public static EmailChannelMessageModel simple(String subjectLine, String content, String providerName, String providerUrl) {
        return new EmailChannelMessageModel(subjectLine, content, "Summary Format", providerName, providerUrl, null, null);
    }

    public static EmailChannelMessageModel project(String subjectLine, String content, String providerName, String providerUrl, ProjectMessage projectMessage) {
        LinkableItem project = projectMessage.getProject();
        return new EmailChannelMessageModel(subjectLine, content, "Standard Format", providerName, providerUrl, project.getValue(), projectMessage);
    }

    private EmailChannelMessageModel(
        String subjectLine,
        String content,
        String messageFormat,
        String providerName,
        String providerUrl,
        @Nullable String projectName,
        @Nullable ProjectMessage source
    ) {
        this.subjectLine = subjectLine;
        this.content = content;
        this.messageFormat = messageFormat;
        this.providerName = providerName;
        this.providerUrl = providerUrl;
        this.projectName = projectName;
        this.source = source;
    }

    public String getSubjectLine() {
        return subjectLine;
    }

    public String getContent() {
        return content;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public Optional<String> getProjectName() {
        return Optional.ofNullable(projectName);
    }

    public Optional<ProjectMessage> getSource() {
        return Optional.ofNullable(source);
    }

}
