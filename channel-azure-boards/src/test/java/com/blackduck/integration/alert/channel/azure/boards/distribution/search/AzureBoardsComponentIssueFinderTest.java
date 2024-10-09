package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.channel.azure.boards.distribution.AzureBoardsIssueTrackerQueryManager;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueComponentUnknownVersionDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueEstimatedRiskModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.AbstractBomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentPolicy;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

class AzureBoardsComponentIssueFinderTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Provider", "A provider", "https://provider-url"));
    private static final LinkableItem PROJECT_ITEM = new LinkableItem("Project", "A project", "https://project-url");
    private static final LinkableItem PROJECT_VERSION_ITEM = new LinkableItem("Project Version", "2.3.4-RC", "https://project-version-url");
    private static final LinkableItem COMPONENT_ITEM = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION_ITEM = new LinkableItem("Component Version", "1.0.0-SNAPSHOT");
    private static final ComponentPolicy COMPONENT_POLICY = new ComponentPolicy("A policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, false, false, null, "Uncategorized");
    private static final ComponentVulnerabilities COMPONENT_VULNERABILITIES = new ComponentVulnerabilities(
        List.of(),
        List.of(),
        List.of(),
        List.of(new LinkableItem("Vulnerability", "CVE-007"))
    );
    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = createBomComponentDetailsWithComponentVulnerabilities(COMPONENT_VULNERABILITIES);
    private static final IssueBomComponentDetails ISSUE_BOM_COMPONENT_DETAILS = IssueBomComponentDetails.fromBomComponentDetails(BOM_COMPONENT_DETAILS);

    private final String workItemCompletedState = "Done";
    private final String workItemReopenState = "Reopen";
    private final AzureBoardsIssueStatusResolver azureBoardsIssueStatusResolver = new AzureBoardsIssueStatusResolver(workItemCompletedState, workItemReopenState);

    @Test
    void findExistingIssuesByProjectIssueModelTest() throws AlertException {
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        String organizationName = "orgName";
        AzureBoardsIssueTrackerQueryManager queryManager = Mockito.mock(AzureBoardsIssueTrackerQueryManager.class);
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();

        AzureBoardsExistingIssueDetailsCreator issueDetailsCreator = new AzureBoardsExistingIssueDetailsCreator(
            organizationName,
            issueCategoryRetriever,
            azureBoardsIssueStatusResolver
        );
        AzureBoardsWorkItemFinder workItemFinder = new AzureBoardsWorkItemFinder(queryManager, "test proj");
        AzureBoardsComponentIssueFinder componentIssueFinder = new AzureBoardsComponentIssueFinder(gson, workItemFinder, issueDetailsCreator);

        IssuePolicyDetails testPolicy = new IssuePolicyDetails("Test Policy", ItemOperation.ADD, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS, testPolicy);

        WorkItemResponseModel workItemResponseModel = createWorkItemResponseModel(workItemReopenState);
        Mockito.when(queryManager.executeQueryAndRetrieveWorkItems(Mockito.any())).thenReturn(List.of(workItemResponseModel));

        IssueTrackerSearchResult<Integer> existingIssueDetailsList = componentIssueFinder.findExistingIssuesByProjectIssueModel(projectIssueModel);

        assertEquals(1, existingIssueDetailsList.getSearchResults().size());
        ExistingIssueDetails<Integer> existingIssueDetails = existingIssueDetailsList.getSearchResults().get(0).getExistingIssueDetails();
        assertEquals(IssueStatus.RESOLVABLE, existingIssueDetails.getIssueStatus());
        assertEquals(IssueCategory.POLICY, existingIssueDetails.getIssueCategory());
    }

    @Test
    void findExistingIssuesByProjectIssueModelForUnknownVersionTest() throws AlertException {
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        String organizationName = "orgName";
        AzureBoardsIssueTrackerQueryManager queryManager = Mockito.mock(AzureBoardsIssueTrackerQueryManager.class);
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();

        AzureBoardsExistingIssueDetailsCreator issueDetailsCreator = new AzureBoardsExistingIssueDetailsCreator(
            organizationName,
            issueCategoryRetriever,
            azureBoardsIssueStatusResolver
        );
        AzureBoardsWorkItemFinder workItemFinder = new AzureBoardsWorkItemFinder(queryManager, "test proj");
        AzureBoardsComponentIssueFinder componentIssueFinder = new AzureBoardsComponentIssueFinder(gson, workItemFinder, issueDetailsCreator);

        IssueComponentUnknownVersionDetails componentUnknownVersionDetails = new IssueComponentUnknownVersionDetails(ItemOperation.ADD, createRiskModels());
        IssuePolicyDetails testPolicy = new IssuePolicyDetails("Test Policy", ItemOperation.ADD, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.componentUnknownVersion(
            PROVIDER_DETAILS,
            PROJECT_ITEM,
            PROJECT_VERSION_ITEM,
            ISSUE_BOM_COMPONENT_DETAILS,
            componentUnknownVersionDetails
        );

        WorkItemResponseModel workItemResponseModel = createWorkItemResponseModel(workItemReopenState);
        Mockito.when(queryManager.executeQueryAndRetrieveWorkItems(Mockito.any())).thenReturn(List.of(workItemResponseModel));

        IssueTrackerSearchResult<Integer> existingIssueDetailsList = componentIssueFinder.findExistingIssuesByProjectIssueModel(projectIssueModel);

        assertEquals(1, existingIssueDetailsList.getSearchResults().size());
        ExistingIssueDetails<Integer> existingIssueDetails = existingIssueDetailsList.getSearchResults().get(0).getExistingIssueDetails();
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
