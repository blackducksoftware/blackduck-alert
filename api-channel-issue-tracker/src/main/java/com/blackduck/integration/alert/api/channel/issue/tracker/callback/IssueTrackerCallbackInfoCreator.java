/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.callback;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;

@Component
public class IssueTrackerCallbackInfoCreator {
    public Optional<IssueTrackerCallbackInfo> createCallbackInfo(ProjectIssueModel projectIssueModel) {
        return projectIssueModel.getProjectVersion()
                   .flatMap(LinkableItem::getUrl)
                   .map(url -> createCallbackInfo(projectIssueModel, url));
    }

    private IssueTrackerCallbackInfo createCallbackInfo(ProjectIssueModel projectIssueModel, String projectVersionUrl) {
        ProviderDetails providerDetails = projectIssueModel.getProviderDetails();
        IssueBomComponentDetails bomComponentDetails = projectIssueModel.getBomComponentDetails();
        return new IssueTrackerCallbackInfo(
            providerDetails.getProviderConfigId(),
            bomComponentDetails.getBlackDuckIssuesUrl(),
            projectVersionUrl
        );
    }

}
