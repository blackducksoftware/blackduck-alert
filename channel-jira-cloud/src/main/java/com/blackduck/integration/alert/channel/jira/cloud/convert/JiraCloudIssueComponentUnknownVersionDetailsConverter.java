/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert;

import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueComponentUnknownVersionDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueEstimatedRiskModel;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;

public class JiraCloudIssueComponentUnknownVersionDetailsConverter {
    private static final String TEXT_COMPONENT_DELETE = "Component was removed or the version was set.";
    private static final String SECTION_LABEL_VULNERABILITY_COUNTS = "Vulnerability counts:";
    private final ChannelMessageFormatter formatter;

    public JiraCloudIssueComponentUnknownVersionDetailsConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
    }

    public void createEstimatedRiskDetailsSectionPieces(IssueComponentUnknownVersionDetails unknownVersionDetails, AtlassianDocumentBuilder documentBuilder) {

        if (ItemOperation.DELETE.equals(unknownVersionDetails.getItemOperation())) {
            documentBuilder.addTextNode(formatter.encode(TEXT_COMPONENT_DELETE));
            documentBuilder.addTextNode(formatter.getLineSeparator());
        } else {
            documentBuilder.addTextNode(formatter.encode(SECTION_LABEL_VULNERABILITY_COUNTS));

            documentBuilder.startBulletList();
            for (IssueEstimatedRiskModel estimatedRiskModel : unknownVersionDetails.getEstimatedRiskModelList()) {
                createEstimatedRiskString(estimatedRiskModel, documentBuilder);
            }
            documentBuilder.finishBulletList();
        }
    }

    private void createEstimatedRiskString(IssueEstimatedRiskModel estimatedRiskModel, AtlassianDocumentBuilder documentBuilder) {
        documentBuilder.addListItem();
        String severity = formatter.encode(estimatedRiskModel.getSeverity().getVulnerabilityLabel());
        String countString = formatter.encode(String.format("(%s)", estimatedRiskModel.getCount()));
        String componentName = formatter.encode(estimatedRiskModel.getName());
        // "    <SEVERITY>: (<COUNT>) <COMPONENT_NAME>"
        final String builder = formatter.getNonBreakingSpace()
            + formatter.getNonBreakingSpace()
            + formatter.getNonBreakingSpace()
            + formatter.getNonBreakingSpace()
            + severity
            + formatter.encode(":")
            + formatter.getNonBreakingSpace()
            + countString
            + formatter.getNonBreakingSpace();
        documentBuilder.addTextNode(builder)
            .addTextNode(componentName, estimatedRiskModel.getComponentVersionUrl().map(formatter::encode).orElse(null));
    }
}
