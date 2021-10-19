package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueComponentUnknownVersionDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueEstimatedRiskModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.distribution.AzureBoardsIssueTrackerQueryManager;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

public class AzureBoardsComponentIssueFinderTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Provider", "A provider", "https://provider-url"));
    private static final LinkableItem PROJECT_ITEM = new LinkableItem("Project", "A project", "https://project-url");
    private static final LinkableItem PROJECT_VERSION_ITEM = new LinkableItem("Project Version", "2.3.4-RC", "https://project-version-url");
    private static final LinkableItem COMPONENT_ITEM = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION_ITEM = new LinkableItem("Component Version", "1.0.0-SNAPSHOT");
    private static final ComponentPolicy COMPONENT_POLICY = new ComponentPolicy("A policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, false, false, null, "Uncategorized");
    private static final ComponentVulnerabilities COMPONENT_VULNERABILITIES = new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of(new LinkableItem("Vulnerability", "CVE-007")));
    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = createBomComponentDetailsWithComponentVulnerabilities(COMPONENT_VULNERABILITIES);
    private static final IssueBomComponentDetails ISSUE_BOM_COMPONENT_DETAILS = IssueBomComponentDetails.fromBomComponentDetails(BOM_COMPONENT_DETAILS);

    private final String workItemCompletedState = "Done";
    private final String workItemReopenState = "Reopen";
    private final AzureBoardsIssueStatusResolver azureBoardsIssueStatusResolver = new AzureBoardsIssueStatusResolver(workItemCompletedState, workItemReopenState);

    @Test
    public void findExistingIssuesByProjectIssueModelTest() throws AlertException {
        Gson gson = new Gson();
        String organizationName = "orgName";
        AzureBoardsIssueTrackerQueryManager queryManager = Mockito.mock(AzureBoardsIssueTrackerQueryManager.class);
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();

        AzureBoardsExistingIssueDetailsCreator issueDetailsCreator = new AzureBoardsExistingIssueDetailsCreator(organizationName, issueCategoryRetriever, azureBoardsIssueStatusResolver);
        AzureBoardsWorkItemFinder workItemFinder = new AzureBoardsWorkItemFinder(queryManager, "test proj");
        AzureBoardsComponentIssueFinder componentIssueFinder = new AzureBoardsComponentIssueFinder(gson, workItemFinder, issueDetailsCreator);

        IssuePolicyDetails testPolicy = new IssuePolicyDetails("Test Policy", ItemOperation.ADD, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS, testPolicy);

        WorkItemResponseModel workItemResponseModel = createWorkItemResponseModel(workItemReopenState);
        Mockito.when(queryManager.executeQueryAndRetrieveWorkItems(Mockito.any())).thenReturn(List.of(workItemResponseModel));

        List<ExistingIssueDetails<Integer>> existingIssueDetailsList = componentIssueFinder.findExistingIssuesByProjectIssueModel(projectIssueModel);

        assertEquals(1, existingIssueDetailsList.size());
        ExistingIssueDetails<Integer> existingIssueDetails = existingIssueDetailsList.get(0);
        assertEquals(IssueStatus.RESOLVABLE, existingIssueDetails.getIssueStatus());
        assertEquals(IssueCategory.POLICY, existingIssueDetails.getIssueCategory());
    }

    @Test
    public void findExistingIssuesByProjectIssueModelForUnknownVersionTest() throws AlertException {
        Gson gson = new Gson();
        String organizationName = "orgName";
        AzureBoardsIssueTrackerQueryManager queryManager = Mockito.mock(AzureBoardsIssueTrackerQueryManager.class);
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();

        AzureBoardsExistingIssueDetailsCreator issueDetailsCreator = new AzureBoardsExistingIssueDetailsCreator(organizationName, issueCategoryRetriever, azureBoardsIssueStatusResolver);
        AzureBoardsWorkItemFinder workItemFinder = new AzureBoardsWorkItemFinder(queryManager, "test proj");
        AzureBoardsComponentIssueFinder componentIssueFinder = new AzureBoardsComponentIssueFinder(gson, workItemFinder, issueDetailsCreator);

        IssueComponentUnknownVersionDetails componentUnknownVersionDetails = new IssueComponentUnknownVersionDetails(ItemOperation.ADD, createRiskModels());
        IssuePolicyDetails testPolicy = new IssuePolicyDetails("Test Policy", ItemOperation.ADD, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.componentUnknownVersion(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS, componentUnknownVersionDetails);

        WorkItemResponseModel workItemResponseModel = createWorkItemResponseModel(workItemReopenState);
        Mockito.when(queryManager.executeQueryAndRetrieveWorkItems(Mockito.any())).thenReturn(List.of(workItemResponseModel));

        List<ExistingIssueDetails<Integer>> existingIssueDetailsList = componentIssueFinder.findExistingIssuesByProjectIssueModel(projectIssueModel);

        assertEquals(1, existingIssueDetailsList.size());
        ExistingIssueDetails<Integer> existingIssueDetails = existingIssueDetailsList.get(0);
        assertEquals(IssueStatus.RESOLVABLE, existingIssueDetails.getIssueStatus());
        assertEquals(IssueCategory.BOM, existingIssueDetails.getIssueCategory());
    }

    private WorkItemResponseModel createWorkItemResponseModel(String workItemState) {
        JsonObject workItemFields = new JsonObject();
        workItemFields.addProperty(WorkItemResponseFields.System_State.getFieldName(), workItemState);
        return new WorkItemResponseModel(1, null, workItemFields, null, null, null, null);
    }

    private static AbstractBomComponentDetails createBomComponentDetailsWithComponentVulnerabilities(ComponentVulnerabilities componentVulnerabilities) {
        return new AbstractBomComponentDetails(
            COMPONENT_ITEM,
            COMPONENT_VERSION_ITEM,
            componentVulnerabilities,
            List.of(COMPONENT_POLICY),
            new LinkableItem("License", "A software license", "https://license-url"),
            "Example Usage",
            ComponentUpgradeGuidance.none(),
            List.of(
                new LinkableItem("Attribute", "Example attribute")
            ),
            "https://issues-url"
        ) {};
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
