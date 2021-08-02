package com.synopsys.integration.alert.api.channel.issue.send;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;

public class IssueTrackerIssueResponseCreatorTest {
    private static final ExistingIssueDetails<String> EXISTING_ISSUE_DETAILS = new ExistingIssueDetails<>("id", "key", "summary", "https://ui-link", IssueStatus.UNKNOWN, IssueCategory.BOM);

    @Test
    public void createIssueResponseTest() {
        IssueTrackerCallbackInfoCreator callbackInfoCreator = Mockito.mock(IssueTrackerCallbackInfoCreator.class);
        Mockito.when(callbackInfoCreator.createCallbackInfo(Mockito.any())).thenReturn(Optional.empty());
        ProjectIssueModel source = Mockito.mock(ProjectIssueModel.class);
        runTest(source, callbackInfoCreator);

    }

    @Test
    public void createIssueResponseWithNullSourceTest() {
        IssueTrackerCallbackInfoCreator callbackInfoCreator = Mockito.mock(IssueTrackerCallbackInfoCreator.class);
        runTest(null, callbackInfoCreator);
    }

    @Test
    public void createIssueResponseWithCallbackInfoTest() {
        IssueTrackerCallbackInfo callbackInfo = Mockito.mock(IssueTrackerCallbackInfo.class);
        IssueTrackerCallbackInfoCreator callbackInfoCreator = Mockito.mock(IssueTrackerCallbackInfoCreator.class);
        Mockito.when(callbackInfoCreator.createCallbackInfo(Mockito.any())).thenReturn(Optional.of(callbackInfo));
        ProjectIssueModel source = Mockito.mock(ProjectIssueModel.class);
        runTest(source, callbackInfoCreator);
    }

    private void runTest(@Nullable ProjectIssueModel source, IssueTrackerCallbackInfoCreator callbackInfoCreator) {
        IssueOperation testOperation = IssueOperation.OPEN;

        IssueTrackerIssueResponseCreator responseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);
        IssueTrackerIssueResponseModel<String> issueResponse = responseCreator.createIssueResponse(source, EXISTING_ISSUE_DETAILS, testOperation);
        assertEquals(EXISTING_ISSUE_DETAILS.getIssueId(), issueResponse.getIssueId());
        assertEquals(EXISTING_ISSUE_DETAILS.getIssueKey(), issueResponse.getIssueKey());
        assertEquals(EXISTING_ISSUE_DETAILS.getIssueSummary(), issueResponse.getIssueTitle());
        assertEquals(testOperation, issueResponse.getIssueOperation());
    }

}
