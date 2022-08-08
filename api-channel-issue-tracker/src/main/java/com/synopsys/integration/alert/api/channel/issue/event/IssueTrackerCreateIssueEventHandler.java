/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.event;

import com.synopsys.integration.alert.api.channel.issue.event.distribution.JobSubTaskEventHandler;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;

public abstract class IssueTrackerCreateIssueEventHandler extends JobSubTaskEventHandler<IssueTrackerCreateIssueEvent> {
    protected IssueTrackerCreateIssueEventHandler(
        EventManager eventManager,
        JobSubTaskAccessor jobSubTaskAccessor
    ) {
        super(eventManager, jobSubTaskAccessor);
    }
}
