package com.blackduck.integration.alert.channel.jira.cloud;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerMessageProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.AsyncMessageSender;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.channel.jira.cloud.convert.JiraCloudModelExtractor;

import java.util.LinkedList;
import java.util.List;

public class JiraCloudProcessor implements IssueTrackerMessageProcessor<String> {
    private final JiraCloudModelExtractor modelExtractor;
    private final AsyncMessageSender<IssueTrackerModelHolder<String>> messageSender;

    public JiraCloudProcessor(JiraCloudModelExtractor modelExtractor, AsyncMessageSender<IssueTrackerModelHolder<String>> messageSender) {
        this.modelExtractor = modelExtractor;
        this.messageSender = messageSender;
    }

    @Override
    public IssueTrackerResponse<String> processMessages(ProviderMessageHolder messages, String jobName) throws AlertException {
        List<IssueTrackerModelHolder<String>> issueTrackerModels = new LinkedList<>();
        IssueTrackerModelHolder<String> simpleMessageHolder = modelExtractor.extractSimpleMessageIssueModels(messages.getSimpleMessages(), jobName);
        issueTrackerModels.add(simpleMessageHolder);

        for (ProjectMessage projectMessage : messages.getProjectMessages()) {
            IssueTrackerModelHolder<String> projectMessageHolder = modelExtractor.extractProjectMessageIssueModels(projectMessage, jobName);
            issueTrackerModels.add(projectMessageHolder);
        }

        messageSender.sendAsyncMessages(issueTrackerModels);

        return new IssueTrackerResponse<>("Success", List.of());
    }
}
