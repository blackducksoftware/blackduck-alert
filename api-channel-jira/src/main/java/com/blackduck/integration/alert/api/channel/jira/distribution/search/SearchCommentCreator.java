package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public final class SearchCommentCreator {

    public static String createSearchComment(LinkableItem project, LinkableItem projectVersion, LinkableItem component, @Nullable LinkableItem componentVersion, @Nullable ComponentConcernType componentConcernType, @Nullable String policyName) {
        String projectId = StringUtils.substringAfterLast(project.getUrl().orElse(""), "/");
        int versionUUIDStart = StringUtils.lastIndexOf(projectVersion.getUrl().orElse(""), "versions/") + "versions/".length();
        int versionUUIDEnd = StringUtils.indexOf(projectVersion.getUrl().orElse(""), "/", versionUUIDStart);
        String projectVersionId = StringUtils.substring(projectVersion.getUrl().orElse(""), versionUUIDStart, versionUUIDEnd);
        String componentName = component.getValue();
        String componentVersionName = null;
        if(null != componentVersion) {
            componentVersionName = componentVersion.getValue();
        }
        return createSearchComment(projectId, projectVersionId, componentName, componentVersionName, componentConcernType, policyName);
    }

    public static String createSearchComment(String projectId, String projectVersionId, String componentName, @Nullable String componentVersion, @Nullable ComponentConcernType componentConcernType, @Nullable String policyName) {
            StringBuilder keyBuilder = new StringBuilder();

        keyBuilder.append("This comment was automatically created by Alert. DO NOT REMOVE.");
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_START_HEADER);
        keyBuilder.append(StringUtils.SPACE);
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_PROJECT_ID);
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_SEPARATOR);
        keyBuilder.append(projectId);
        keyBuilder.append(StringUtils.SPACE);
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_PROJECT_VERSION_ID);
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_SEPARATOR);
        keyBuilder.append(projectVersionId);
        keyBuilder.append(StringUtils.SPACE);
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_COMPONENT_NAME);
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_SEPARATOR);
        keyBuilder.append(componentName);
        keyBuilder.append(StringUtils.SPACE);
        if(StringUtils.isNotBlank(componentVersion)) {
            keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_COMPONENT_VERSION_NAME);
            keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_SEPARATOR);
            keyBuilder.append(componentVersion);
            keyBuilder.append(StringUtils.SPACE);
        }

        if(null != componentConcernType) {
            keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_CATEGORY);
            keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_SEPARATOR);
            keyBuilder.append(componentConcernType.name());
            keyBuilder.append(StringUtils.SPACE);
        }

        if(StringUtils.isNotBlank(policyName)) {
            // add policy name
            keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_POLICY_NAME);
            keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_SEPARATOR);
            keyBuilder.append(JiraIssueSearchPropertyStringCompatibilityUtils.createPolicyAdditionalKey(policyName));
            keyBuilder.append(StringUtils.SPACE);
        }
        keyBuilder.append(StringUtils.SPACE);
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_END_HEADER);

        return keyBuilder.toString();
    }

    private SearchCommentCreator() {
        // cannot instantiate this class.
    }

}
