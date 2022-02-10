package com.synopsys.integration.alert.channel.jira.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;

@Component
public class JiraServerTestActionFactory {
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final Gson gson;

    @Autowired
    public JiraServerTestActionFactory(JiraServerPropertiesFactory jiraServerPropertiesFactory, Gson gson) {
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.gson = gson;
    }

    public JiraServerGlobalTestActionWrapper createTestActionWrapper(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) throws IssueTrackerException {
        return new JiraServerGlobalTestActionWrapper(jiraServerPropertiesFactory, gson, jiraServerGlobalConfigModel);
    }
}
