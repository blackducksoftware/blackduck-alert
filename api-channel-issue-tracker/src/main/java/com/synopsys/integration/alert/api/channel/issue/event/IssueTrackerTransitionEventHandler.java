/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.event;

import java.io.Serializable;

import com.synopsys.integration.alert.api.channel.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.distribution.JobSubTaskEventHandler;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;

public abstract class IssueTrackerTransitionEventHandler<T extends JobSubTaskEvent> extends JobSubTaskEventHandler<T> {

    private final IssueTrackerResponsePostProcessor responsePostProcessor;

    protected IssueTrackerTransitionEventHandler(
        EventManager eventManager,
        JobSubTaskAccessor jobSubTaskAccessor,
        IssueTrackerResponsePostProcessor responsePostProcessor
    ) {
        super(eventManager, jobSubTaskAccessor, JobStage.ISSUE_RESOLVING);
        this.responsePostProcessor = responsePostProcessor;
    }

    protected <T extends Serializable> void postProcess(IssueTrackerResponse<T> response) {
        responsePostProcessor.postProcess(response);
    }
}
