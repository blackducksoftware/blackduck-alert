package com.blackduck.integration.alert.api.channel.issue.tracker.event;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.distribution.JobSubTaskEventHandler;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.distribution.JobSubTaskEvent;

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
