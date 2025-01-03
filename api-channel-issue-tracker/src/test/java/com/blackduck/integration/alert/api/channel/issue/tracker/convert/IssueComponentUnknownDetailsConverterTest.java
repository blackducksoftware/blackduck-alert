/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.issue.tracker.convert.mock.MockIssueTrackerMessageFormatter;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueComponentUnknownVersionDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueEstimatedRiskModel;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;

public class IssueComponentUnknownDetailsConverterTest {

    @Test
    public void createComponentUnknownDetailsAddTest() {
        IssueComponentUnknownVersionDetails details = createDetails(ItemOperation.ADD);
        List<String> sectionPieces = callCreateSectionPieces(details);
        int headerSize = 3;
        int severityCounts = (2 * details.getEstimatedRiskModelList().size());
        assertEquals(headerSize + severityCounts, sectionPieces.size());
    }

    @Test
    public void createComponentUnknownDetailsDeleteTest() {
        List<String> sectionPieces = callCreateSectionPieces(createDetails(ItemOperation.DELETE));

        assertEquals(2, sectionPieces.size());
    }

    @Disabled
    @Test
    public void previewFromatting() {
        List<String> sectionPieces = callCreateSectionPieces(createDetails(ItemOperation.ADD));
        String joinedSectionPieces = StringUtils.join(sectionPieces, "");
        System.out.print(joinedSectionPieces);
    }

    private List<String> callCreateSectionPieces(IssueComponentUnknownVersionDetails details) {
        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        IssueComponentUnknownVersionDetailsConverter converter = new IssueComponentUnknownVersionDetailsConverter(formatter);

        return converter.createEstimatedRiskDetailsSectionPieces(details);
    }

    private IssueComponentUnknownVersionDetails createDetails(ItemOperation itemOperation) {
        return new IssueComponentUnknownVersionDetails(itemOperation, createRiskModels());
    }

    private List<IssueEstimatedRiskModel> createRiskModels() {
        List<IssueEstimatedRiskModel> riskModels = new ArrayList<>(ComponentConcernSeverity.values().length);
        for (ComponentConcernSeverity severity : ComponentConcernSeverity.values()) {
            if (!ComponentConcernSeverity.UNSPECIFIED_UNKNOWN.equals(severity)) {
                riskModels.add(new IssueEstimatedRiskModel(severity, severity.ordinal(), "Component 1.0.0", "https://www.example.com/api/component/1-0-0"));
            }
        }

        return riskModels;
    }
}
