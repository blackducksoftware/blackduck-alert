/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.callback;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;

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
