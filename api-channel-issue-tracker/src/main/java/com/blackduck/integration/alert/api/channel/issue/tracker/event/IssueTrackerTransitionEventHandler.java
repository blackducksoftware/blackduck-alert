/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.event;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.distribution.JobSubTaskEventHandler;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.distribution.JobSubTaskEvent;

public abstract class IssueTrackerTransitionEventHandler<T extends JobSubTaskEvent> extends JobSubTaskEventHandler<T> {

    private final IssueTrackerResponsePostProcessor responsePostProcessor;

    protected IssueTrackerTransitionEventHandler(
        EventManager eventManager,
        IssueTrackerResponsePostProcessor responsePostProcessor,
        ExecutingJobManager executingJobManager
    ) {
        super(eventManager, JobStage.ISSUE_TRANSITION, executingJobManager);
        this.responsePostProcessor = responsePostProcessor;
    }

    protected <T extends Serializable> void postProcess(IssueTrackerResponse<T> response) {
        responsePostProcessor.postProcess(response);
    }
}
