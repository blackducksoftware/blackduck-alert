package com.synopsys.integration.alert.api.channel.issue.convert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.convert.mock.MockIssueTrackerMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;

public class IssuePolicyDetailsConverterTest {
    private static final ComponentPolicy COMPONENT_POLICY = createComponentPolicy("A vulnerability policy", "Description of a policy");
    private static final ComponentPolicy COMPONENT_POLICY_NO_DESCRIPTION = createComponentPolicy("policy without description", null);

    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = new AbstractBomComponentDetails(
        new LinkableItem("Component", "A BOM component"),
        new LinkableItem("Component Version", "1.0.0-SNAPSHOT"),
        new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of()),
        List.of(COMPONENT_POLICY, COMPONENT_POLICY_NO_DESCRIPTION),
        new LinkableItem("License", "A software license", "https://license-url"),
        "Example Usage",
        ComponentUpgradeGuidance.none(),
        List.of(),
        "https://issues-url"
    ) {};
    private static final IssueBomComponentDetails ISSUE_BOM_COMPONENT_DETAILS = IssueBomComponentDetails.fromBomComponentDetails(BOM_COMPONENT_DETAILS);

    private static final IssuePolicyDetails POLICY_DETAILS_WITH_VULNERABILITY = new IssuePolicyDetails(COMPONENT_POLICY.getPolicyName(), ItemOperation.ADD, COMPONENT_POLICY.getSeverity());
    private static final IssuePolicyDetails POLICY_DETAILS_WITHOUT_VULNERABILITY = new IssuePolicyDetails("Normal Policy", ItemOperation.DELETE, ComponentConcernSeverity.TRIVIAL_LOW);
    private static final IssuePolicyDetails POLICY_DETAILS_WITHOUT_DESCRIPTION = new IssuePolicyDetails(COMPONENT_POLICY_NO_DESCRIPTION.getPolicyName(), ItemOperation.DELETE, ComponentConcernSeverity.TRIVIAL_LOW);

    @Test
    public void createPolicyDetailsSectionPiecesWithVulnerabilityTest() {
        callCreatePolicyDetailsSectionPieces(POLICY_DETAILS_WITH_VULNERABILITY);
    }

    @Disabled
    @Test
    public void previewFormattingWithVulnerability() {
        List<String> sectionPieces = callCreatePolicyDetailsSectionPieces(POLICY_DETAILS_WITH_VULNERABILITY);
        String joinedSectionPieces = StringUtils.join(sectionPieces, "");
        System.out.print(joinedSectionPieces);
    }

    @Test
    public void createPolicyDetailsSectionPiecesWithoutVulnerabilityTest() {
        callCreatePolicyDetailsSectionPieces(POLICY_DETAILS_WITHOUT_VULNERABILITY);
    }

    @Disabled
    @Test
    public void previewFormattingWithoutVulnerability() {
        List<String> sectionPieces = callCreatePolicyDetailsSectionPieces(POLICY_DETAILS_WITHOUT_VULNERABILITY);
        String joinedSectionPieces = StringUtils.join(sectionPieces, "");
        System.out.print(joinedSectionPieces);
    }

    @Test
    public void createPolicyDetailsSectionWithoutDescriptionTest() {
        callCreatePolicyDetailsSectionPieces(POLICY_DETAILS_WITHOUT_DESCRIPTION);
    }

    @Disabled
    @Test
    public void previewFromattingWithoutDescription() {
        List<String> sectionPieces = callCreatePolicyDetailsSectionPieces(POLICY_DETAILS_WITHOUT_DESCRIPTION);
        String joinedSectionPieces = StringUtils.join(sectionPieces, "");
        System.out.print(joinedSectionPieces);
    }

    private List<String> callCreatePolicyDetailsSectionPieces(IssuePolicyDetails policyDetails) {
        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        IssuePolicyDetailsConverter converter = new IssuePolicyDetailsConverter(formatter);
        return converter.createPolicyDetailsSectionPieces(ISSUE_BOM_COMPONENT_DETAILS, policyDetails);
    }

    private static ComponentPolicy createComponentPolicy(String policyName, String description) {
        return new ComponentPolicy(
            policyName,
            ComponentConcernSeverity.MINOR_MEDIUM,
            false,
            true,
            description,
            "Uncategorized"
        );
    }

}
