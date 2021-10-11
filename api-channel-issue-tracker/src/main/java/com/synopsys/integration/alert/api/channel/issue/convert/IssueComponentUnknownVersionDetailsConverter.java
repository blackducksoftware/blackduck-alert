/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.convert;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueComponentUnknownVersionDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueEstimatedRiskModel;

public class IssueComponentUnknownVersionDetailsConverter {

    private static final String SECTION_LABEL_VULNERABILITY_COUNTS = "Vulnerability counts:";
    private final ChannelMessageFormatter formatter;

    public IssueComponentUnknownVersionDetailsConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
    }

    public List<String> createEstimatedRiskDetailsSectionPieces(IssueComponentUnknownVersionDetails unknownVersionDetails) {
        List<String> estimatedRiskSectionPieces = new LinkedList<>();

        estimatedRiskSectionPieces.add(formatter.encode(SECTION_LABEL_VULNERABILITY_COUNTS));
        estimatedRiskSectionPieces.add(formatter.getLineSeparator());
        estimatedRiskSectionPieces.add(formatter.getLineSeparator());

        for (IssueEstimatedRiskModel estimatedRiskModel : unknownVersionDetails.getEstimatedRiskModelList()) {
            estimatedRiskSectionPieces.add(createEstimatedRiskString(estimatedRiskModel));
            estimatedRiskSectionPieces.add(formatter.getLineSeparator());
        }
        return estimatedRiskSectionPieces;
    }

    private String createEstimatedRiskString(IssueEstimatedRiskModel estimatedRiskModel) {
        String severity = formatter.encode(estimatedRiskModel.getSeverity().getVulnerabilityLabel());
        String countString = formatter.encode(String.format("(%s)", formatter.getNonBreakingSpace(), estimatedRiskModel.getCount()));
        String componentName = createComponentNameLinkIfPresent(estimatedRiskModel);
        // "    <SEVERITY>: (<COUNT>) <COMPONENT_NAME>"
        StringBuilder builder = new StringBuilder(100);
        builder.append(formatter.getNonBreakingSpace());
        builder.append(formatter.getNonBreakingSpace());
        builder.append(formatter.getNonBreakingSpace());
        builder.append(formatter.getNonBreakingSpace());
        builder.append(severity);
        builder.append(formatter.getNonBreakingSpace());
        builder.append(countString);
        builder.append(formatter.getNonBreakingSpace());
        builder.append(componentName);

        return builder.toString();
    }

    private String createComponentNameLinkIfPresent(IssueEstimatedRiskModel estimatedRiskModel) {
        String encodedName = formatter.encode(estimatedRiskModel.getName());
        String componentNameString = encodedName;

        Optional<String> optionalUrl = estimatedRiskModel.getComponentVersionUrl();

        if (optionalUrl.isPresent()) {
            String encodedUrl = formatter.encode(optionalUrl.get());
            componentNameString = formatter.createLink(encodedName, encodedUrl);
        }

        return componentNameString;
    }
}
