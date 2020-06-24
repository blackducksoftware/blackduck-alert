package com.synopsys.integration.alert.provider.blackduck.issues;

import java.time.OffsetDateTime;

import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.IssueView;
import com.synopsys.integration.blackduck.service.BlackDuckService;

public class BlackDuckProviderIssueHandler {
    private final BlackDuckService blackDuckService;

    public BlackDuckProviderIssueHandler(BlackDuckService blackDuckService) {
        this.blackDuckService = blackDuckService;
    }

    public void createOrUpdateBlackDuckIssue(String blackDuckIssuesUrl, BlackDuckProviderIssueModel issueModel) {
        // FIXME implement
    }

    private IssueView createIssueView(BlackDuckProviderIssueModel issueModel, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        IssueView blackDuckIssueView = new IssueView();
        blackDuckIssueView.setIssueId(issueModel.getKey());
        blackDuckIssueView.setIssueLink(issueModel.getLink());
        blackDuckIssueView.setIssueAssignee(issueModel.getAssignee());
        blackDuckIssueView.setIssueStatus(issueModel.getStatus());
        blackDuckIssueView.setIssueDescription(issueModel.getSummary());

        // FIXME blackDuckIssueView.setIssueCreatedAt(jiraIssue.getCreated());
        // FIXME blackDuckIssueView.setIssueUpdatedAt(jiraIssue.getUpdated());
        return blackDuckIssueView;
    }

}
