package com.synopsys.integration.alert.api.channel.issue.send;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;

public class IssueTrackerIssueCreatorTest {
    private static final IssueTrackerChannelKey ISSUE_TRACKER_CHANNEL_KEY = new IssueTrackerChannelKey("key", "name") {};

    private static IssueTrackerIssueCommenter<String> commenter;
    private static IssueTrackerCallbackInfoCreator callbackInfoCreator;

    @BeforeAll
    public static void init() throws AlertException {
        commenter = Mockito.mock(IssueTrackerIssueCommenter.class);
        Mockito.when(commenter.commentOnIssue(Mockito.any())).thenReturn(Optional.empty());

        callbackInfoCreator = Mockito.mock(IssueTrackerCallbackInfoCreator.class);
        Mockito.when(callbackInfoCreator.createCallbackInfo(Mockito.any())).thenReturn(Optional.empty());
    }

    @Test
    public void createIssueTrackerIssueTest() throws AlertException {
        TestIssueCreator issueCreator = new TestIssueCreator(commenter, callbackInfoCreator);
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("Test title", null, List.of("comment 1", "comment 2"), null);
        IssueTrackerIssueResponseModel<String> responseModel = issueCreator.createIssueTrackerIssue(issueCreationModel);
        assertEquals(issueCreationModel.getTitle(), responseModel.getIssueTitle());
    }

    @Test
    public void createIssueTrackerIssueWithSourceTest() throws AlertException {
        TestIssueCreator issueCreator = new TestIssueCreator(commenter, callbackInfoCreator);
        ProjectIssueModel projectIssueModel = Mockito.mock(ProjectIssueModel.class);
        IssueCreationModel issueCreationModel = IssueCreationModel.project("Test title", null, List.of("example comment"), projectIssueModel);
        IssueTrackerIssueResponseModel<String> responseModel = issueCreator.createIssueTrackerIssue(issueCreationModel);
        assertEquals(issueCreationModel.getTitle(), responseModel.getIssueTitle());
    }

    private static class TestIssueCreator extends IssueTrackerIssueCreator<String> {
        public TestIssueCreator(IssueTrackerIssueCommenter<String> commenter, IssueTrackerCallbackInfoCreator callbackInfoCreator) {
            super(ISSUE_TRACKER_CHANNEL_KEY, commenter, callbackInfoCreator);
        }

        @Override
        protected ExistingIssueDetails<String> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) {
            return new ExistingIssueDetails<>(null, null, alertIssueCreationModel.getTitle(), null, IssueStatus.UNKNOWN, IssueCategory.BOM);
        }

        @Override
        protected void assignAlertSearchProperties(ExistingIssueDetails<String> createdIssueDetails, ProjectIssueModel alertIssueSource) {
            // Do nothing
        }

    }

}
