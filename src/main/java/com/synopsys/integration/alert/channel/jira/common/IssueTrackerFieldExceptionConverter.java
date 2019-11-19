package com.synopsys.integration.alert.channel.jira.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.issuetracker.exception.IssueTrackerFieldException;
import com.synopsys.integration.alert.issuetracker.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.issuetracker.jira.server.JiraServerProperties;

@Component
public class IssueTrackerFieldExceptionConverter {
    private final Map<String, String> keyLookupMap = Map.of(
        JiraCloudProperties.KEY_ISSUE_CREATOR, JiraDescriptor.KEY_ISSUE_CREATOR,
        JiraCloudProperties.KEY_JIRA_PROJECT_NAME, JiraDescriptor.KEY_JIRA_PROJECT_NAME,
        JiraCloudProperties.KEY_ISSUE_TYPE, JiraDescriptor.KEY_ISSUE_TYPE,
        JiraCloudProperties.KEY_OPEN_WORKFLOW_TRANSITION, JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION,
        JiraCloudProperties.KEY_RESOLVE_WORKFLOW_TRANSITION, JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION,
        JiraServerProperties.KEY_ISSUE_CREATOR, JiraServerDescriptor.KEY_ISSUE_CREATOR,
        JiraServerProperties.KEY_JIRA_PROJECT_NAME, JiraServerDescriptor.KEY_JIRA_PROJECT_NAME,
        JiraServerProperties.KEY_ISSUE_TYPE, JiraServerDescriptor.KEY_ISSUE_TYPE,
        JiraServerProperties.KEY_OPEN_WORKFLOW_TRANSITION, JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION,
        JiraServerProperties.KEY_RESOLVE_WORKFLOW_TRANSITION, JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION);

    public final AlertFieldException convert(IssueTrackerFieldException ex) {
        Map<String, String> errorsMap = new HashMap<>();
        for (Map.Entry<String, String> errorEntry : ex.getFieldErrors().entrySet()) {
            String errorKey = errorEntry.getKey();
            String key = keyLookupMap.getOrDefault(errorKey, errorKey);
            errorsMap.put(key, errorEntry.getValue());
        }

        return new AlertFieldException(errorsMap);
    }

}
