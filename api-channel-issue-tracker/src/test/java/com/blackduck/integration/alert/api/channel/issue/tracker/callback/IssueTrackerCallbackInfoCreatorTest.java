package com.blackduck.integration.alert.api.channel.issue.tracker.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.AbstractBomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;
import com.blackduck.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class IssueTrackerCallbackInfoCreatorTest {
    private static final LinkableItem TEST_ITEM = new LinkableItem("Test Label", "Test Value");
    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = new AbstractBomComponentDetails(
        TEST_ITEM,
        TEST_ITEM,
        new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of()),
        List.of(),
        TEST_ITEM,
        "Example Usage",
        ComponentUpgradeGuidance.none(),
        List.of(),
        "https://issues-url"
    ) {};

    private static final LinkableItem PROVIDER_ITEM = new LinkableItem("Provider", "Test Provider", "https://provider-url");
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(0L, PROVIDER_ITEM);
    private static final IssueBomComponentDetails ISSUE_BOM_COMPONENT_DETAILS = IssueBomComponentDetails.fromBomComponentDetails(BOM_COMPONENT_DETAILS);

    @Test
    public void createCallbackInfoNoProjectVersionUrlTest() {
        LinkableItem projectVersionNoUrl = new LinkableItem("Project Version", "A Project Version", null);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(PROVIDER_DETAILS, TEST_ITEM, projectVersionNoUrl, ISSUE_BOM_COMPONENT_DETAILS);

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        Optional<IssueTrackerCallbackInfo> callbackInfo = callbackInfoCreator.createCallbackInfo(projectIssueModel);
        assertTrue(callbackInfo.isEmpty(), "Expected no callback info to be present because no project-version url was present");
    }

    @Test
    public void createCallbackInfoWithProjectVersionUrlTest() {
        LinkableItem projectVersionWithUrl = new LinkableItem("Project Version", "A Project Version", "https://project-version-url");
        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(PROVIDER_DETAILS, TEST_ITEM, projectVersionWithUrl, ISSUE_BOM_COMPONENT_DETAILS);

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        Optional<IssueTrackerCallbackInfo> optionalCallbackInfo = callbackInfoCreator.createCallbackInfo(projectIssueModel);
        assertTrue(optionalCallbackInfo.isPresent(), "Expected a callback info to be present because a project-version url was present");
        IssueTrackerCallbackInfo callbackInfo = optionalCallbackInfo.get();
        assertEquals(PROVIDER_DETAILS.getProviderConfigId(), callbackInfo.getProviderConfigId());
        assertEquals(BOM_COMPONENT_DETAILS.getBlackDuckIssuesUrl(), callbackInfo.getCallbackUrl());
        assertEquals(projectVersionWithUrl.getUrl().orElse(null), callbackInfo.getBlackDuckProjectVersionUrl());
    }

}
