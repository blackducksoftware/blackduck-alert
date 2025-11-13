/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.channel.issue.tracker.convert.IssueTrackerMessageFormatter;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueComponentUnknownVersionDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueVulnerabilityDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.channel.message.ChunkedStringBuilder;
import com.blackduck.integration.alert.common.channel.message.ChunkedStringBuilderRechunker;
import com.blackduck.integration.alert.common.channel.message.RechunkedModel;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;

public class JiraCloudProjectIssueModelConverter {
    public static final int COMPONENT_CONCERN_TITLE_SECTION_CHAR_COUNT = 20;
    public static final LinkableItem MISSING_PROJECT_VERSION_PLACEHOLDER = new LinkableItem("Project Version", "Unknown");
    public static final String DESCRIPTION_CONTINUED_TEXT = "(description continued...)";
    public static final String COMMA_SPACE = ", ";
    public static final String LABEL_SEVERITY_STATUS = "Severity Status: ";

    private final IssueTrackerMessageFormatter formatter;
    private final JiraCloudBomComponentDetailConverter bomComponentDetailConverter;
    private final JiraCloudIssuePolicyDetailsConverter issuePolicyDetailsConverter;
    private final JiraCloudIssueVulnerabilityDetailsConverter issueVulnerabilityDetailsConverter;
    private final JiraCloudIssueComponentUnknownVersionDetailsConverter issueComponentUnknownVersionDetailsConverter;
    private final JiraCloudComponentVulnerabilitiesConverter componentVulnerabilitiesConverter;

    public JiraCloudProjectIssueModelConverter(IssueTrackerMessageFormatter formatter) {
        this.formatter = formatter;
        this.bomComponentDetailConverter = new JiraCloudBomComponentDetailConverter(formatter);
        this.issuePolicyDetailsConverter = new JiraCloudIssuePolicyDetailsConverter(formatter);
        this.issueVulnerabilityDetailsConverter = new JiraCloudIssueVulnerabilityDetailsConverter(formatter);
        this.issueComponentUnknownVersionDetailsConverter = new JiraCloudIssueComponentUnknownVersionDetailsConverter(formatter);
        this.componentVulnerabilitiesConverter = new JiraCloudComponentVulnerabilitiesConverter(formatter);
    }

    public IssueCreationModel toIssueCreationModel(ProjectIssueModel projectIssueModel, String jobName, String queryString) {
        String title = createTruncatedTitle(projectIssueModel);
        AtlassianDocumentBuilder documentBuilder = new AtlassianDocumentBuilder(formatter, true);
        ChunkedStringBuilder descriptionBuilder = new ChunkedStringBuilder(formatter.getMaxDescriptionLength());

        String nonBreakingSpace = formatter.getNonBreakingSpace();
        String jobLine = String.format("Job%sname:%s%s", nonBreakingSpace, nonBreakingSpace, jobName);
        documentBuilder.addTextNode(jobLine, true)
            .addParagraphNode()
            .addTextNode(projectIssueModel.getProject(), true)
            .addParagraphNode()
            .addTextNode(projectIssueModel.getProjectVersion().orElse(MISSING_PROJECT_VERSION_PLACEHOLDER), true)
            .addParagraphNode()
            .addTextNode(formatter.getSectionSeparator(), false)
            .addParagraphNode();

        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();
        bomComponentDetailConverter.gatherAbstractBomComponentSectionPieces(bomComponent, documentBuilder);

        createVulnerabilitySeverityStatusSectionPieces(projectIssueModel, documentBuilder);

        documentBuilder.addParagraphNode();
        createProjectIssueModelConcernSectionPieces(projectIssueModel, documentBuilder, false);

        int newChunkSize = formatter.getMaxCommentLength() - DESCRIPTION_CONTINUED_TEXT.length() - formatter.getLineSeparator().length();
        RechunkedModel rechunkedDescription = ChunkedStringBuilderRechunker.rechunk(descriptionBuilder, "No description", newChunkSize);

        List<String> postCreateComments = rechunkedDescription.getRemainingChunks()
            .stream()
            .map(comment -> String.format("%s%s%s", DESCRIPTION_CONTINUED_TEXT, formatter.getLineSeparator(), comment))
            .toList();

        AtlassianDocumentFormatModel description = documentBuilder.buildPrimaryDocument();
        List<AtlassianDocumentFormatModel> additionalComments = documentBuilder.buildAdditionalCommentDocuments();

        return IssueCreationModel.project(title, rechunkedDescription.getFirstChunk(), postCreateComments, projectIssueModel, description, additionalComments, queryString);
    }

    public <T extends Serializable> IssueTransitionModel<T> toIssueTransitionModel(
        ExistingIssueDetails<T> existingIssueDetails,
        ProjectIssueModel projectIssueModel,
        ItemOperation requiredOperation
    ) {
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
        AtlassianDocumentBuilder documentBuilder = new AtlassianDocumentBuilder(formatter, false);

        LinkableItem provider = projectIssueModel.getProvider();
        String commentHeader = String.format("The component was updated in %s[%s]", provider.getLabel(), provider.getValue());
        documentBuilder
            .addTextNode(formatter.encode(commentHeader))
            .addTextNode(formatter.getSectionSeparator())
            .addParagraphNode();

        createVulnerabilitySeverityStatusSectionPieces(projectIssueModel, documentBuilder);

        createProjectIssueModelConcernSectionPieces(projectIssueModel, documentBuilder, true);

        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();
        bomComponentDetailConverter.gatherAttributeStrings(bomComponent, documentBuilder);

        AtlassianDocumentFormatModel primaryComment = documentBuilder.buildPrimaryDocument();
        List<AtlassianDocumentFormatModel> additionalComments = documentBuilder.buildAdditionalCommentDocuments();
        return new IssueCommentModel<>(existingIssueDetails, List.of(), projectIssueModel, primaryComment, additionalComments);
    }

    private String createTruncatedTitle(ProjectIssueModel projectIssueModel) {
        LinkableItem provider = projectIssueModel.getProvider();
        LinkableItem project = projectIssueModel.getProject();
        LinkableItem projectVersion = projectIssueModel.getProjectVersion().orElse(MISSING_PROJECT_VERSION_PLACEHOLDER);

        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();
        LinkableItem component = bomComponent.getComponent();
        Optional<String> optionalComponentVersionValue = bomComponent.getComponentVersion().map(LinkableItem::getValue);
        boolean isComponentVersionUnknown = projectIssueModel.getComponentUnknownVersionDetails().isPresent();
        StringBuilder componentPieceBuilder = new StringBuilder();
        componentPieceBuilder.append(component.getValue());

        if (optionalComponentVersionValue.isPresent() && !isComponentVersionUnknown) {
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
        } else if (isComponentVersionUnknown) {
            componentConcernPieceBuilder.append(COMMA_SPACE);
            componentConcernPieceBuilder.append(ComponentConcernType.UNKNOWN_VERSION.getDisplayName());
        } else {
            componentConcernPieceBuilder.append(COMMA_SPACE);
            componentConcernPieceBuilder.append(ComponentConcernType.VULNERABILITY.getDisplayName());
        }

        String componentConcernPiece = componentConcernPieceBuilder.toString();

        String preConcernTitle = String.format(
            "Alert - %s[%s], %s[%s], %s",
            provider.getLabel(),
            provider.getValue(),
            project.getValue(),
            projectVersion.getValue(),
            componentPieceBuilder
        );
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

    private void createProjectIssueModelConcernSectionPieces(ProjectIssueModel projectIssueModel, AtlassianDocumentBuilder documentBuilder, boolean commentFormat) {

        IssueBomComponentDetails bomComponentDetails = projectIssueModel.getBomComponentDetails();

        Optional<IssuePolicyDetails> optionalPolicyDetails = projectIssueModel.getPolicyDetails();
        if (optionalPolicyDetails.isPresent()) {
            issuePolicyDetailsConverter.createPolicyDetailsSectionPieces(bomComponentDetails, optionalPolicyDetails.get(), documentBuilder);
            documentBuilder
                .addTextNode(formatter.getLineSeparator())
                .addTextNode(formatter.getSectionSeparator())
                .addTextNode(formatter.getLineSeparator());
        }

        Optional<IssueVulnerabilityDetails> optionalVulnDetails = projectIssueModel.getVulnerabilityDetails();
        if (optionalVulnDetails.isPresent()) {
            if (commentFormat) {
                issueVulnerabilityDetailsConverter.createVulnerabilityDetailsSectionPieces(optionalVulnDetails.get(), documentBuilder);
            } else {
                componentVulnerabilitiesConverter.createComponentVulnerabilitiesSectionPieces(
                    projectIssueModel.getBomComponentDetails().getComponentVulnerabilities(),
                    documentBuilder
                );
            }

            documentBuilder
                .addTextNode(formatter.getLineSeparator())
                .addTextNode(formatter.getSectionSeparator())
                .addTextNode(formatter.getLineSeparator());
        }

        Optional<IssueComponentUnknownVersionDetails> optionalUnknownVersionDetails = projectIssueModel.getComponentUnknownVersionDetails();
        if (optionalUnknownVersionDetails.isPresent()) {
            issueComponentUnknownVersionDetailsConverter.createEstimatedRiskDetailsSectionPieces(optionalUnknownVersionDetails.get(), documentBuilder);

            documentBuilder
                .addTextNode(formatter.getLineSeparator())
                .addTextNode(formatter.getSectionSeparator())
                .addTextNode(formatter.getLineSeparator());
        }

    }

    private void createVulnerabilitySeverityStatusSectionPieces(ProjectIssueModel projectIssueModel, AtlassianDocumentBuilder documentBuilder) {
        String encodedSeverityStatus = formatter.encode(LABEL_SEVERITY_STATUS);
        IssueBomComponentDetails bomComponentDetails = projectIssueModel.getBomComponentDetails();

        Optional<IssueVulnerabilityDetails> vulnerabilityDetails = projectIssueModel.getVulnerabilityDetails();
        if (vulnerabilityDetails.isPresent()) {
            ComponentVulnerabilities componentVulnerabilities = bomComponentDetails.getComponentVulnerabilities();
            componentVulnerabilities.computeHighestSeverity()
                .map(ComponentConcernSeverity::getVulnerabilityLabel)
                .map(formatter::encode)
                .map(severity -> encodedSeverityStatus + severity)
                .ifPresentOrElse(documentBuilder::addTextNode, () -> documentBuilder.addTextNode(encodedSeverityStatus + "None"));
            documentBuilder
                .addTextNode(formatter.getLineSeparator())
                .addTextNode(formatter.getSectionSeparator())
                .addTextNode(formatter.getLineSeparator());
            documentBuilder.addParagraphNode();
        }
    }

}
