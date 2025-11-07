package com.blackduck.integration.alert.channel.jira.cloud.send;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.AsyncMessageSender;

import java.util.List;

public class JiraCloudAsyncMessageSender implements AsyncMessageSender<IssueTrackerModelHolder<String>> {

    @Override
    public void sendAsyncMessages(List<IssueTrackerModelHolder<String>> issueTrackerMessages) {

    }
}
