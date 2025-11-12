/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import java.util.List;
import java.util.Optional;

import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class IssueCreationModel extends AlertSerializableModel {
    private static final long serialVersionUID = -3568919050386494416L;
    private final String queryString;
    private final String title;
    private final String description;
    private final List<String> postCreateComments;

    private final LinkableItem provider;
    private final ProjectIssueModel source;
    private final AtlassianDocumentFormatModel atlassianDocumentFormatDescriptionModel;
    private final List<AtlassianDocumentFormatModel> atlassianDocumentFormatCommentModel;

    public static IssueCreationModel simple(String title, String description, List<String> postCreateComments, LinkableItem provider) {
        return new IssueCreationModel(title, description, postCreateComments, provider, null, null, null, null);
    }

    public static IssueCreationModel simple(String title, LinkableItem provider, AtlassianDocumentFormatModel atlassianDocumentFormatDescriptionModel, List<AtlassianDocumentFormatModel> atlassianDocumentFormatCommentModel) {
        return new IssueCreationModel(title, "", List.of(), provider, null, null, atlassianDocumentFormatDescriptionModel, atlassianDocumentFormatCommentModel);
    }


    public static IssueCreationModel project(String title, String description, List<String> postCreateComments, ProjectIssueModel source, @Nullable String queryString) {
        return new IssueCreationModel(title, description, postCreateComments, source.getProvider(), source, queryString, null, null);
    }

    public static IssueCreationModel project(String title, String description, List<String> postCreateComments, ProjectIssueModel source, AtlassianDocumentFormatModel atlassianDocumentFormatDescriptionModel, List<AtlassianDocumentFormatModel> atlassianDocumentFormatCommentModel, @Nullable String queryString) {
        return new IssueCreationModel(title, description, postCreateComments, source.getProvider(), source, queryString, atlassianDocumentFormatDescriptionModel, atlassianDocumentFormatCommentModel);
    }

    protected IssueCreationModel(
        String title,
        String description,
        List<String> postCreateComments,
        LinkableItem provider,
        @Nullable ProjectIssueModel source,
        @Nullable String queryString,
        @Nullable AtlassianDocumentFormatModel atlassianDocumentFormatDescriptionModel,
        @Nullable List<AtlassianDocumentFormatModel> atlassianDocumentFormatCommentModel
    ) {
        this.title = title;
        this.description = description;
        this.postCreateComments = postCreateComments;
        this.provider = provider;
        this.source = source;
        this.queryString = queryString;
        this.atlassianDocumentFormatDescriptionModel = atlassianDocumentFormatDescriptionModel;
        this.atlassianDocumentFormatCommentModel = atlassianDocumentFormatCommentModel;
    }

    public Optional<String> getQueryString() {
        return Optional.ofNullable(queryString);
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

    public Optional<AtlassianDocumentFormatModel> getAtlassianDocumentFormatDescriptionModel() {
        return Optional.ofNullable(atlassianDocumentFormatDescriptionModel);
    }

    public Optional<List<AtlassianDocumentFormatModel>> getAtlassianDocumentFormatCommentModel() {
        return Optional.ofNullable(atlassianDocumentFormatCommentModel);
    }
}
