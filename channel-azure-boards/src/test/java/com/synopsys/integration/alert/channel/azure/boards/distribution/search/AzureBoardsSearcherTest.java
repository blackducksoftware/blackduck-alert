package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.api.channel.issue.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
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
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

public class AzureBoardsSearcherTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Provider", "A provider", "https://provider-url"));
    private static final LinkableItem PROJECT_ITEM = new LinkableItem("Project", "A project", "https://project-url");
    private static final LinkableItem PROJECT_VERSION_ITEM = new LinkableItem("Project Version", "2.3.4-RC", "https://project-version-url");
    private static final LinkableItem COMPONENT_ITEM = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION_ITEM = new LinkableItem("Component Version", "1.0.0-SNAPSHOT");
    private static final ComponentPolicy COMPONENT_POLICY = new ComponentPolicy("A policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, false, false);
    private static final ComponentVulnerabilities COMPONENT_VULNERABILITIES = new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of(new LinkableItem("Vulnerability", "CVE-007")));
    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = createBomComponentDetailsWithComponentVulnerabilities(COMPONENT_VULNERABILITIES);
    private static final IssueBomComponentDetails ISSUE_BOM_COMPONENT_DETAILS = IssueBomComponentDetails.fromBomComponentDetails(BOM_COMPONENT_DETAILS);
    private static final ExistingIssueDetails<String> EXISTING_ISSUE_DETAILS = new ExistingIssueDetails<>("issue-id", "issue-key", "a summary", "https://ui-link", IssueStatus.UNKNOWN, IssueCategory.BOM);

    private final String workItemCompletedState = "Done";
    private final String workItemReopenState = "Reopen";
    private final AzureBoardsIssueStatusResolver azureBoardsIssueStatusResolver = new AzureBoardsIssueStatusResolver(workItemCompletedState, workItemReopenState);

    @Test
    public void findExistingIssuesByProjectIssueModelTest() throws AlertException {
        Gson gson = new Gson();
        String organizationName = "orgName";
        ProjectMessageToIssueModelTransformer modelTransformer = Mockito.mock(ProjectMessageToIssueModelTransformer.class);
        AzureBoardsIssueTrackerQueryManager azureBoardsIssueTrackerQueryManager = Mockito.mock(AzureBoardsIssueTrackerQueryManager.class);
        AzureBoardsSearcher azureBoardsSearcher = new AzureBoardsSearcher(gson, organizationName, azureBoardsIssueTrackerQueryManager, modelTransformer, azureBoardsIssueStatusResolver, issueCategoryRetriever);

        IssuePolicyDetails testPolicy = new IssuePolicyDetails("Test Policy", ItemOperation.ADD, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS, testPolicy);

        WorkItemResponseModel workItemResponseModel = createWorkItemResponseModel(workItemReopenState);
        Mockito.when(azureBoardsIssueTrackerQueryManager.executeQueryAndRetrieveWorkItems(Mockito.any())).thenReturn(List.of(workItemResponseModel));

        List<ExistingIssueDetails<Integer>> existingIssueDetailsList = azureBoardsSearcher.findExistingIssuesByProjectIssueModel(projectIssueModel);

        assertEquals(1, existingIssueDetailsList.size());
        ExistingIssueDetails<Integer> existingIssueDetails = existingIssueDetailsList.get(0);
        assertEquals(IssueStatus.RESOLVABLE, existingIssueDetails.getIssueStatus());
        assertEquals(IssueCategory.POLICY, existingIssueDetails.getIssueCategory());
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
            List.of(
                new LinkableItem("Attribute", "Example attribute")
            ),
            "https://issues-url"
        ) {};
    }
}
