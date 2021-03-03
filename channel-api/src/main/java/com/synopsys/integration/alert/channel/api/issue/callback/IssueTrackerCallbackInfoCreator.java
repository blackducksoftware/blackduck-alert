/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.callback;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;

@Component
public class IssueTrackerCallbackInfoCreator {
    public IssueTrackerCallbackInfo createCallbackInfo(ProjectIssueModel projectIssueModel) {
        ProviderDetails providerDetails = projectIssueModel.getProviderDetails();
        IssueBomComponentDetails bomComponentDetails = projectIssueModel.getBomComponentDetails();
        String projectVersionUrl = projectIssueModel.getProjectVersion()
                                       .flatMap(LinkableItem::getUrl)
                                       .orElseThrow(() -> new AlertRuntimeException("Missing project-version url"));
        return new IssueTrackerCallbackInfo(
            providerDetails.getProviderConfigId(),
            bomComponentDetails.getBlackDuckIssuesUrl(),
            projectVersionUrl
        );
    }

}
