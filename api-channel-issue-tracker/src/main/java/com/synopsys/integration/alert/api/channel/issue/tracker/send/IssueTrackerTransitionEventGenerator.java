/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.tracker.send;

import java.io.Serializable;

import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;

public interface IssueTrackerTransitionEventGenerator<T extends Serializable> {
    IssueTrackerTransitionIssueEvent<T> generateEvent(IssueTransitionModel<T> model);
}
