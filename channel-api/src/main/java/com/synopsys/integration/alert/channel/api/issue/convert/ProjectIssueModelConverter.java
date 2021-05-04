/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.convert;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.channel.api.convert.BomComponentDetailConverter;
import com.synopsys.integration.alert.channel.api.convert.ComponentVulnerabilitiesConverter;
import com.synopsys.integration.alert.channel.api.convert.LinkableItemConverter;
import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueVulnerabilityDetails;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilder;
import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilderRechunker;
import com.synopsys.integration.alert.common.channel.message.RechunkedModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

public class ProjectIssueModelConverter {
    public static final int COMPONENT_CONCERN_TITLE_SECTION_CHAR_COUNT = 20;
    public static final LinkableItem MISSING_PROJECT_VERSION_PLACEHOLDER = new LinkableItem("Project Version", "Unknown");
    public static final String DESCRIPTION_CONTINUED_TEXT = "(description continued...)";
    public static final String COMMA_SPACE = ", ";

    private final IssueTrackerMessageFormatter formatter;
    private final BomComponentDetailConverter bomComponentDetailConverter;
    private final IssuePolicyDetailsConverter issuePolicyDetailsConverter;
    private final IssueVulnerabilityDetailsConverter issueVulnerabilityDetailsConverter;
    private final ComponentVulnerabilitiesConverter componentVulnerabilitiesConverter;
    private final LinkableItemConverter linkableItemConverter;

    public ProjectIssueModelConverter(IssueTrackerMessageFormatter formatter) {
        this.formatter = formatter;
        this.bomComponentDetailConverter = new BomComponentDetailConverter(formatter);
        this.issuePolicyDetailsConverter = new IssuePolicyDetailsConverter(formatter);
        this.issueVulnerabilityDetailsConverter = new IssueVulnerabilityDetailsConverter(formatter);
        this.componentVulnerabilitiesConverter = new ComponentVulnerabilitiesConverter(formatter);
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

        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();
        List<String> bomComponentPieces = bomComponentDetailConverter.gatherAbstractBomComponentSectionPieces(bomComponent);
        bomComponentPieces.forEach(descriptionBuilder::append);

        descriptionBuilder.append(formatter.getLineSeparator());
        createProjectIssueModelConcernSectionPieces(projectIssueModel, false)
            .forEach(descriptionBuilder::append);

        int newChunkSize = formatter.getMaxCommentLength() - DESCRIPTION_CONTINUED_TEXT.length() - formatter.getLineSeparator().length();
        RechunkedModel rechunkedDescription = ChunkedStringBuilderRechunker.rechunk(descriptionBuilder, "No description", newChunkSize);

        List<String> postCreateComments = rechunkedDescription.getRemainingChunks()
                                              .stream()
                                              .map(comment -> String.format("%s%s%s", DESCRIPTION_CONTINUED_TEXT, formatter.getLineSeparator(), comment))
                                              .collect(Collectors.toList());

        return IssueCreationModel.project(title, rechunkedDescription.getFirstChunk(), postCreateComments, projectIssueModel);
    }

    public <T extends Serializable> IssueTransitionModel<T> toIssueTransitionModel(ExistingIssueDetails<T> existingIssueDetails, ProjectIssueModel projectIssueModel, ItemOperation requiredOperation) {
        IssueOperation issueOperation;
        if (ItemOperation.ADD.equals(requiredOperation)) {
            issueOperation = IssueOperation.OPEN;
        } else {
            issueOperation = IssueOperation.RESOLVE;
        }

        IssueCommentModel<T> commentModel = toIssueCommentModel(existingIssueDetails, projectIssueModel);
        List<String> transitionComments = new LinkedList<>(commentModel.getComments());

        LinkableItem provider = projectIssueModel.getProvider();
        ChunkedStringBuilder commentBuilder = new ChunkedStringBuilder(formatter.getMaxCommentLength());
        commentBuilder.append(String.format("The %s operation was performed on this component in %s.", requiredOperation.name(), provider.getLabel()));

        transitionComments.addAll(commentBuilder.collectCurrentChunks());

        return new IssueTransitionModel<>(existingIssueDetails, issueOperation, transitionComments, projectIssueModel);
    }

    public <T extends Serializable> IssueCommentModel<T> toIssueCommentModel(ExistingIssueDetails<T> existingIssueDetails, ProjectIssueModel projectIssueModel) {
        ChunkedStringBuilder commentBuilder = new ChunkedStringBuilder(formatter.getMaxCommentLength());

        LinkableItem provider = projectIssueModel.getProvider();
        String commentHeader = String.format("The component was updated in %s[%s]", provider.getLabel(), provider.getValue());
        commentBuilder.append(formatter.encode(commentHeader));
        commentBuilder.append(formatter.getLineSeparator());
        commentBuilder.append(formatter.getSectionSeparator());
        commentBuilder.append(formatter.getLineSeparator());

        createProjectIssueModelConcernSectionPieces(projectIssueModel, true)
            .forEach(commentBuilder::append);

        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();
        List<String> attributeStrings = bomComponentDetailConverter.gatherAttributeStrings(bomComponent);
        for (String attributeString : attributeStrings) {
            commentBuilder.append(formatter.getNonBreakingSpace());
            commentBuilder.append(formatter.encode("-"));
            commentBuilder.append(formatter.getNonBreakingSpace());
            commentBuilder.append(attributeString);
            commentBuilder.append(formatter.getLineSeparator());
        }

        List<String> chunkedComments = commentBuilder.collectCurrentChunks();
        return new IssueCommentModel<>(existingIssueDetails, chunkedComments, projectIssueModel);
    }

    private String createTruncatedTitle(ProjectIssueModel projectIssueModel) {
        LinkableItem provider = projectIssueModel.getProvider();
        LinkableItem project = projectIssueModel.getProject();
        LinkableItem projectVersion = projectIssueModel.getProjectVersion().orElse(MISSING_PROJECT_VERSION_PLACEHOLDER);

        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();
        LinkableItem component = bomComponent.getComponent();
        Optional<String> optionalComponentVersionValue = bomComponent.getComponentVersion().map(LinkableItem::getValue);

        StringBuilder componentPieceBuilder = new StringBuilder();
        componentPieceBuilder.append(component.getValue());

        if (optionalComponentVersionValue.isPresent()) {
            componentPieceBuilder.append('[');
            componentPieceBuilder.append(optionalComponentVersionValue.get());
            componentPieceBuilder.append(']');
        }

        StringBuilder componentConcernPieceBuilder = new StringBuilder();

        Optional<String> optionalPolicyName = projectIssueModel.getPolicyDetails().map(IssuePolicyDetails::getName);
        if (optionalPolicyName.isPresent()) {
            componentConcernPieceBuilder.append(COMMA_SPACE);
            componentConcernPieceBuilder.append(ComponentConcernType.POLICY.getDisplayName());
            componentConcernPieceBuilder.append('[');
            componentConcernPieceBuilder.append(optionalPolicyName.get());
            componentConcernPieceBuilder.append(']');
        } else {
            componentConcernPieceBuilder.append(COMMA_SPACE);
            componentConcernPieceBuilder.append(ComponentConcernType.VULNERABILITY.getDisplayName());
        }

        String componentConcernPiece = componentConcernPieceBuilder.toString();

        String preConcernTitle = String.format("Alert - %s[%s], %s[%s], %s", provider.getLabel(), provider.getValue(), project.getValue(), projectVersion.getValue(), componentPieceBuilder);
        if (preConcernTitle.length() + componentConcernPieceBuilder.length() > formatter.getMaxTitleLength()) {
            if (formatter.getMaxTitleLength() > COMPONENT_CONCERN_TITLE_SECTION_CHAR_COUNT) {
                preConcernTitle = StringUtils.truncate(preConcernTitle, formatter.getMaxTitleLength() - COMPONENT_CONCERN_TITLE_SECTION_CHAR_COUNT);
                componentConcernPiece = StringUtils.truncate(componentConcernPieceBuilder.toString(), COMPONENT_CONCERN_TITLE_SECTION_CHAR_COUNT);
            } else {
                // If max title length is less than 3, then there are bigger concerns than an IllegalArgumentException
                preConcernTitle = StringUtils.truncate(preConcernTitle, formatter.getMaxTitleLength() - 3);
                componentConcernPiece = "...";
            }
        }

        return preConcernTitle + componentConcernPiece;
    }

    private List<String> createProjectIssueModelConcernSectionPieces(ProjectIssueModel projectIssueModel, boolean commentFormat) {
        List<String> concernSectionPieces = new LinkedList<>();

        IssueBomComponentDetails bomComponentDetails = projectIssueModel.getBomComponentDetails();

        Optional<IssuePolicyDetails> optionalPolicyDetails = projectIssueModel.getPolicyDetails();
        if (optionalPolicyDetails.isPresent()) {
            List<String> policyDetailsSectionPieces = issuePolicyDetailsConverter.createPolicyDetailsSectionPieces(bomComponentDetails, optionalPolicyDetails.get());
            concernSectionPieces.addAll(policyDetailsSectionPieces);
            concernSectionPieces.add(formatter.getLineSeparator());
            concernSectionPieces.add(formatter.getSectionSeparator());
            concernSectionPieces.add(formatter.getLineSeparator());
        }

        Optional<IssueVulnerabilityDetails> optionalVulnDetails = projectIssueModel.getVulnerabilityDetails();
        if (optionalVulnDetails.isPresent()) {
            List<String> vulnDetailsSectionPieces;
            if (commentFormat) {
                vulnDetailsSectionPieces = issueVulnerabilityDetailsConverter.createVulnerabilityDetailsSectionPieces(optionalVulnDetails.get());
            } else {
                vulnDetailsSectionPieces = componentVulnerabilitiesConverter.createComponentVulnerabilitiesSectionPieces(projectIssueModel.getBomComponentDetails().getComponentVulnerabilities());
            }
            concernSectionPieces.addAll(vulnDetailsSectionPieces);
            concernSectionPieces.add(formatter.getLineSeparator());
            concernSectionPieces.add(formatter.getSectionSeparator());
            concernSectionPieces.add(formatter.getLineSeparator());
        }

        return concernSectionPieces;
    }

}
