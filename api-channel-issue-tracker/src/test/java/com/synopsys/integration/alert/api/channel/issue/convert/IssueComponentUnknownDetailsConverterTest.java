package com.synopsys.integration.alert.api.channel.issue.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.convert.mock.MockIssueTrackerMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueComponentUnknownVersionDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueEstimatedRiskModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;

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
