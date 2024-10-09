package com.blackduck.integration.alert.channel.jira.server.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerChannel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

@Component
public class JiraServerChannel extends IssueTrackerChannel<JiraServerJobDetailsModel, String> {
    @Autowired
    protected JiraServerChannel(JiraServerProcessorFactory processorFactory) {
        super(processorFactory);
    }

}
