/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.tracker.event;

import java.io.Serializable;

import com.synopsys.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.distribution.JobSubTaskEventHandler;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;

public abstract class IssueTrackerCommentEventHandler<T extends JobSubTaskEvent> extends JobSubTaskEventHandler<T> {
    private IssueTrackerResponsePostProcessor responsePostProcessor;

    protected IssueTrackerCommentEventHandler(
        EventManager eventManager,
        IssueTrackerResponsePostProcessor responsePostProcessor,
        ExecutingJobManager executingJobManager
    ) {
        super(eventManager, JobStage.ISSUE_COMMENTING, executingJobManager);
        this.responsePostProcessor = responsePostProcessor;
    }

    protected <T extends Serializable> void postProcess(IssueTrackerResponse<T> response) {
        responsePostProcessor.postProcess(response);
    }
}
