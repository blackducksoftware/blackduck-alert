/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.model;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class IssueCreationModel extends AlertSerializableModel {
    private final String title;
    private final String description;
    private final List<String> postCreateComments;

    private final LinkableItem provider;
    private final ProjectIssueModel source;

    public static IssueCreationModel simple(String title, String description, List<String> postCreateComments, LinkableItem provider) {
        return new IssueCreationModel(title, description, postCreateComments, provider, null);
    }

    public static IssueCreationModel project(String title, String description, List<String> postCreateComments, ProjectIssueModel source) {
        return new IssueCreationModel(title, description, postCreateComments, source.getProvider(), source);
    }

    private IssueCreationModel(String title, String description, List<String> postCreateComments, LinkableItem provider, @Nullable ProjectIssueModel source) {
        this.title = title;
        this.description = description;
        this.postCreateComments = postCreateComments;
        this.provider = provider;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPostCreateComments() {
        return postCreateComments;
    }

    public LinkableItem getProvider() {
        return provider;
    }

    public Optional<ProjectIssueModel> getSource() {
        return Optional.ofNullable(source);
    }

}
