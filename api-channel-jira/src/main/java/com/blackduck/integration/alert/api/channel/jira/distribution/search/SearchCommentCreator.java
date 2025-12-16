package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Optional;

public final class SearchCommentCreator {

    public static String createSearchComment(LinkableItem provider, LinkableItem project, LinkableItem projectVersion, LinkableItem component, @Nullable LinkableItem componentVersion, @Nullable ComponentConcernType componentConcernType, @Nullable String policyName) {
        String providerName = provider.getLabel();
        String projectId = project.getValue();
        String projectVersionId = projectVersion.getValue();
        String componentName = component.getValue();
        String componentVersionName = null;
        String category = Optional.ofNullable(componentConcernType).map(ComponentConcernType::name).orElse(null);
        if(null != componentVersion) {
            componentVersionName = componentVersion.getValue();
        }
        return createSearchComment(providerName, projectId, projectVersionId, componentName, componentVersionName, category, policyName);
    }

    public static String createSearchComment(String providerId, String projectId, String projectVersionId, String componentName, @Nullable String componentVersion, @Nullable String category, @Nullable String policyName) {
            StringBuilder keyBuilder = new StringBuilder();

        keyBuilder.append("This comment was automatically created by Alert. DO NOT REMOVE.");
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_START_HEADER);
        keyBuilder.append(StringUtils.SPACE);
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_PROVIDER);
        keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_SEPARATOR);
        keyBuilder.append(providerId);
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

        if(StringUtils.isNotBlank(category)) {
            keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_CATEGORY);
            keyBuilder.append(JiraIssuePropertyKeys.JIRA_ISSUE_KEY_SEPARATOR);
            keyBuilder.append(category);
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
