/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.send;

import java.io.Serializable;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;

public interface IssueTrackerCommentEventGenerator<T extends Serializable> {

    IssueTrackerCommentEvent<T> generateEvent(IssueCommentModel<T> model);

}
