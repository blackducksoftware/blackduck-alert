/*
 * channel-api
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
package com.synopsys.integration.alert.channel.api.issue;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.channel.api.convert.BomComponentDetailConverter;
import com.synopsys.integration.alert.channel.api.convert.LinkableItemConverter;
import com.synopsys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTransitionType;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilder;
import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilderRechunker;
import com.synopsys.integration.alert.common.channel.message.RechunkedModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

public class ProjectIssueModelConverter {
    public static final int COMPONENT_CONCERN_TITLE_SPACE = 20;
    public static final LinkableItem MISSING_PROJECT_VERSION_PLACEHOLDER = new LinkableItem("Project Version", "Unknown");

    private final IssueTrackerMessageFormatter formatter;
    private final BomComponentDetailConverter bomComponentDetailConverter;
    private final LinkableItemConverter linkableItemConverter;

    public ProjectIssueModelConverter(IssueTrackerMessageFormatter formatter) {
        this.formatter = formatter;
        this.bomComponentDetailConverter = new BomComponentDetailConverter(formatter);
        this.linkableItemConverter = new LinkableItemConverter(formatter);
    }

    public IssueCreationModel toIssueCreationModel(ProjectIssueModel projectIssueModel) {
        String title = createTruncatedTitle(projectIssueModel);

        ChunkedStringBuilder descriptionBuilder = new ChunkedStringBuilder(formatter.getMaxDescriptionLength());

        String projectString = linkableItemConverter.convertToString(projectIssueModel.getProject(), true);
        descriptionBuilder.append(projectString);
        descriptionBuilder.append(formatter.getLineSeparator());

        LinkableItem projectVersion = projectIssueModel.getProjectVersion().orElse(MISSING_PROJECT_VERSION_PLACEHOLDER);
        String projectVersionString = linkableItemConverter.convertToString(projectVersion, true);
        descriptionBuilder.append(projectVersionString);
        descriptionBuilder.append(formatter.getLineSeparator());
        descriptionBuilder.append(formatter.getSectionSeparator());
        descriptionBuilder.append(formatter.getLineSeparator());

        BomComponentDetails bomComponent = projectIssueModel.getBomComponent();
        List<String> bomComponentPieces = bomComponentDetailConverter.gatherBomComponentPieces(bomComponent);
        bomComponentPieces.forEach(descriptionBuilder::append);

        RechunkedModel rechunkedDescription = ChunkedStringBuilderRechunker.rechunk(descriptionBuilder, "No description", formatter.getMaxCommentLength());

        return IssueCreationModel.project(title, rechunkedDescription.getFirstChunk(), rechunkedDescription.getRemainingChunks(), projectIssueModel);
    }

    public <T extends Serializable> IssueTransitionModel<T> toIssueTransitionModel(T issueId, ProjectIssueModel projectIssueModel, ItemOperation requiredOperation) {
        IssueTransitionType transitionType;
        if (ItemOperation.ADD.equals(requiredOperation)) {
            transitionType = IssueTransitionType.REOPEN;
        } else {
            transitionType = IssueTransitionType.RESOLVE;
        }

        ChunkedStringBuilder commentBuilder = new ChunkedStringBuilder(formatter.getMaxCommentLength());

        ProviderDetails providerDetails = projectIssueModel.getProviderDetails();
        LinkableItem provider = providerDetails.getProvider();
        commentBuilder.append(String.format("The %s operation was performed on this component in %s.", requiredOperation.name(), provider.getLabel()));

        List<String> chunkedComments = commentBuilder.collectCurrentChunks();
        return new IssueTransitionModel<>(issueId, transitionType, chunkedComments, projectIssueModel);
    }

    public <T extends Serializable> IssueCommentModel<T> toIssueCommentModel(T issueId, ProjectIssueModel projectIssueModel) {
        ChunkedStringBuilder commentBuilder = new ChunkedStringBuilder(formatter.getMaxCommentLength());

        ProviderDetails providerDetails = projectIssueModel.getProviderDetails();
        LinkableItem provider = providerDetails.getProvider();
        commentBuilder.append(String.format("The component was updated in %s:", provider.getLabel()));
        commentBuilder.append(formatter.getLineSeparator());
        commentBuilder.append(formatter.getSectionSeparator());

        bomComponentDetailConverter.createComponentConcernSectionPieces(projectIssueModel.getBomComponent())
            .forEach(commentBuilder::append);

        commentBuilder.append(formatter.getSectionSeparator());
        commentBuilder.append(formatter.getLineSeparator());
        BomComponentDetails bomComponent = projectIssueModel.getBomComponent();
        List<String> attributeStrings = bomComponentDetailConverter.gatherAttributeStrings(bomComponent);
        for (String attributeString : attributeStrings) {
            commentBuilder.append(String.format("%s-%s%s", formatter.getNonBreakingSpace(), formatter.getNonBreakingSpace(), attributeString));
            commentBuilder.append(formatter.getLineSeparator());
        }

        List<String> chunkedComments = commentBuilder.collectCurrentChunks();
        return new IssueCommentModel<>(issueId, chunkedComments, projectIssueModel);
    }

    private String createTruncatedTitle(ProjectIssueModel projectIssueModel) {
        ProviderDetails providerDetails = projectIssueModel.getProviderDetails();
        LinkableItem provider = providerDetails.getProvider();
        LinkableItem project = projectIssueModel.getProject();
        LinkableItem projectVersion = projectIssueModel.getProjectVersion().orElse(MISSING_PROJECT_VERSION_PLACEHOLDER);

        BomComponentDetails bomComponent = projectIssueModel.getBomComponent();
        LinkableItem component = bomComponent.getComponent();
        Optional<LinkableItem> optionalComponentVersion = bomComponent.getComponentVersion();

        StringBuilder componentPieceBuilder = new StringBuilder();
        componentPieceBuilder.append(component.getValue());

        if (optionalComponentVersion.isPresent()) {
            componentPieceBuilder.append('[');
            componentPieceBuilder.append(optionalComponentVersion);
            componentPieceBuilder.append(']');
        }

        Optional<ComponentConcern> arbitraryComponentConcern = bomComponent.getComponentConcerns()
                                                                   .stream()
                                                                   .findAny();

        StringBuilder componentConcernPieceBuilder = new StringBuilder();
        if (arbitraryComponentConcern.isPresent()) {
            ComponentConcern componentConcern = arbitraryComponentConcern.get();
            ComponentConcernType componentConcernType = componentConcern.getType();

            componentConcernPieceBuilder.append(", ");
            componentConcernPieceBuilder.append(componentConcernType.name());

            if (ComponentConcernType.POLICY.equals(componentConcernType)) {
                componentConcernPieceBuilder.append(": ");
                componentConcernPieceBuilder.append(componentConcern.getName());
            }
        }

        String componentConcernPiece = componentConcernPieceBuilder.toString();

        String preConcernTitle = String.format("Alert - %s[%s], %s[%s], %s", provider.getLabel(), provider.getValue(), project.getValue(), projectVersion.getValue(), componentPieceBuilder.toString());
        if (preConcernTitle.length() + componentConcernPieceBuilder.length() > formatter.getMaxTitleLength()) {
            preConcernTitle = StringUtils.truncate(preConcernTitle, formatter.getMaxTitleLength() - COMPONENT_CONCERN_TITLE_SPACE);
            componentConcernPiece = StringUtils.truncate(componentConcernPieceBuilder.toString(), COMPONENT_CONCERN_TITLE_SPACE);
        }

        return preConcernTitle + componentConcernPiece;
    }

}
