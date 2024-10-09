package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import java.io.Serializable;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;

public class IssueTrackerIssueResponseCreator {
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;

    public IssueTrackerIssueResponseCreator(IssueTrackerCallbackInfoCreator callbackInfoCreator) {
        this.callbackInfoCreator = callbackInfoCreator;
    }

    public final <T extends Serializable> IssueTrackerIssueResponseModel<T> createIssueResponse(@Nullable ProjectIssueModel source, ExistingIssueDetails<T> existingIssueDetails, IssueOperation issueOperation) {
        IssueTrackerCallbackInfo callbackInfo = Optional.ofNullable(source)
                                                    .flatMap(callbackInfoCreator::createCallbackInfo)
                                                    .orElse(null);
        return new IssueTrackerIssueResponseModel<>(
            existingIssueDetails.getIssueId(),
            existingIssueDetails.getIssueKey(),
            existingIssueDetails.getIssueUILink(),
            existingIssueDetails.getIssueSummary(),
            issueOperation,
            callbackInfo
        );
    }

}
