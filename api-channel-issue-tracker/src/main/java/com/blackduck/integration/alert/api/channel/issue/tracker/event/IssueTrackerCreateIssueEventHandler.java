package com.blackduck.integration.alert.api.channel.issue.tracker.event;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.distribution.JobSubTaskEventHandler;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.api.event.EventManager;

public abstract class IssueTrackerCreateIssueEventHandler extends JobSubTaskEventHandler<IssueTrackerCreateIssueEvent> {
    private final IssueTrackerResponsePostProcessor responsePostProcessor;

    protected IssueTrackerCreateIssueEventHandler(
        EventManager eventManager,
        IssueTrackerResponsePostProcessor responsePostProcessor,
        ExecutingJobManager executingJobManager
    ) {
        super(eventManager, JobStage.ISSUE_CREATION, executingJobManager);
        this.responsePostProcessor = responsePostProcessor;
    }

    protected <T extends Serializable> void postProcess(IssueTrackerResponse<T> response) {
        responsePostProcessor.postProcess(response);
    }
}
