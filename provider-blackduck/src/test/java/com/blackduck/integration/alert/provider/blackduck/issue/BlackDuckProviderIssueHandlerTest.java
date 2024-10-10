package com.blackduck.integration.alert.provider.blackduck.issue;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.blackduck.api.core.ResourceMetadata;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionIssuesView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.dataservice.IssueService;
import com.blackduck.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;
import com.google.gson.Gson;

@ExtendWith(SpringExtension.class)
class BlackDuckProviderIssueHandlerTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    @Mock
    private BlackDuckApiClient blackDuckApiClient;
    @Mock
    private IssueService issueService;

    @Test
    void projectVersionwithComponentsEmptyTest() throws IntegrationException {
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectVersionView.class)))
            .thenReturn(new ProjectVersionView());
        Mockito.when(issueService.getIssuesForProjectVersion(Mockito.any(ProjectVersionView.class))).thenReturn(List.of());
        BlackDuckProviderIssueHandler handler = new BlackDuckProviderIssueHandler(gson, blackDuckApiClient, issueService);
        BlackDuckProviderIssueModel issueModel = createModel();
        handler.createOrUpdateBlackDuckIssue(issueModel, null, "https://blackduck.example.com/api/project/1/version/2/components");
        Mockito.verify(blackDuckApiClient, Mockito.times(0)).execute(Mockito.any(BlackDuckResponseRequest.class));
    }

    @Test
    void projectVersionwithComponentsTest() throws IntegrationException {
        BlackDuckProviderIssueModel issueModel = createModel();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectVersionView.class)))
            .thenReturn(new ProjectVersionView());
        Mockito.when(issueService.getIssuesForProjectVersion(Mockito.any(ProjectVersionView.class))).thenReturn(List.of(createIssueView(issueModel)));
        BlackDuckProviderIssueHandler handler = new BlackDuckProviderIssueHandler(gson, blackDuckApiClient, issueService);
        handler.createOrUpdateBlackDuckIssue(issueModel, null, "https://blackduck.example.com/api/project/1/version/2/components");
        Mockito.verify(blackDuckApiClient).execute(Mockito.any(BlackDuckResponseRequest.class));
    }

    @Test
    void projectVersionwithoutComponentsTest() throws IntegrationException {
        BlackDuckProviderIssueModel issueModel = createModel();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectVersionView.class)))
            .thenReturn(new ProjectVersionView());
        Mockito.when(issueService.getIssuesForProjectVersion(Mockito.any(ProjectVersionView.class))).thenReturn(List.of(createIssueView(issueModel)));
        BlackDuckProviderIssueHandler handler = new BlackDuckProviderIssueHandler(gson, blackDuckApiClient, issueService);
        handler.createOrUpdateBlackDuckIssue(issueModel, null, "https://blackduck.example.com/api/project/1/version/2");
        Mockito.verify(blackDuckApiClient).execute(Mockito.any(BlackDuckResponseRequest.class));
    }

    @Test
    void projectVersionwithIssuesUrlTest() throws IntegrationException {
        BlackDuckProviderIssueModel issueModel = createModel();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectVersionView.class)))
            .thenReturn(new ProjectVersionView());
        Mockito.when(issueService.getIssuesForProjectVersion(Mockito.any(ProjectVersionView.class))).thenReturn(List.of(createIssueView(issueModel)));
        BlackDuckProviderIssueHandler handler = new BlackDuckProviderIssueHandler(gson, blackDuckApiClient, issueService);
        handler.createOrUpdateBlackDuckIssue(
            issueModel,
            "https://blackduck.example.com/api/project/1/version/2/components/3",
            "https://blackduck.example.com/api/project/1/version/2/components"
        );
        Mockito.verify(blackDuckApiClient).execute(Mockito.any(BlackDuckResponseRequest.class));
    }

    private BlackDuckProviderIssueModel createModel() {
        return new BlackDuckProviderIssueModel("key", "Status", "Summary", "https://blackduck.example.com/api/project/1/version/2/issues");
    }

    private ProjectVersionIssuesView createIssueView(BlackDuckProviderIssueModel issueModel) {
        ProjectVersionIssuesView issuesView = new ProjectVersionIssuesView();
        issuesView.setIssueId(issueModel.getKey());
        issuesView.setIssueDescription(issueModel.getSummary());
        issuesView.setIssueAssignee(issueModel.getAssignee());
        issuesView.setIssueStatus(issueModel.getStatus());
        issuesView.setIssueUpdatedAt(Date.from(Instant.now()));
        issuesView.setMeta(new ResourceMetadata());

        return issuesView;
    }

}
