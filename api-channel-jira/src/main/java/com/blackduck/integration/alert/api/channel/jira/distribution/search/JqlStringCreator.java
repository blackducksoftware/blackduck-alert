/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public final class JqlStringCreator {
    private static final String SEARCH_CONJUNCTION = "AND";
    private static final Set<Character> CHARACTERS_TO_ESCAPE = Set.of('\'', '\\');

    public static String createBlackDuckProjectIssuesSearchString(
        String jiraProjectKey,
        LinkableItem provider,
        LinkableItem project
    ) {
        StringBuilder jqlBuilder = new StringBuilder();
        appendBlackDuckCommentSearchStrings(jqlBuilder, jiraProjectKey, project, null, null, null, null, null);
        jqlBuilder.append(" OR ");
        jqlBuilder.append("(");
        appendBlackDuckProjectSearchStrings(jqlBuilder, jiraProjectKey, provider, project);
        jqlBuilder.append(")");

        return jqlBuilder.toString();
    }

    public static String createBlackDuckProjectVersionIssuesSearchString(
        String jiraProjectKey,
        LinkableItem provider,
        LinkableItem project,
        LinkableItem projectVersion
    ) {
        StringBuilder jqlBuilder = new StringBuilder();
        appendBlackDuckCommentSearchStrings(jqlBuilder, jiraProjectKey, project, projectVersion, null, null, null, null);
        jqlBuilder.append(" OR ");
        jqlBuilder.append("(");
        appendBlackDuckProjectVersionSearchStrings(jqlBuilder, jiraProjectKey, provider, project, projectVersion);
        jqlBuilder.append(")");

        return jqlBuilder.toString();
    }

    public static String createBlackDuckComponentIssuesSearchString(
        String jiraProjectKey,
        LinkableItem provider,
        LinkableItem project,
        LinkableItem projectVersion,
        LinkableItem component,
        @Nullable LinkableItem componentVersion
    ) {
        StringBuilder jqlBuilder = new StringBuilder();
        appendBlackDuckCommentSearchStrings(jqlBuilder, jiraProjectKey, project, projectVersion, component, componentVersion, null, null);
        jqlBuilder.append(" OR ");
        jqlBuilder.append("(");
        appendBlackDuckComponentSearchStrings(jqlBuilder, jiraProjectKey, provider, project, projectVersion, component, componentVersion);
        jqlBuilder.append(")");

        return jqlBuilder.toString();
    }

    public static String createBlackDuckComponentConcernIssuesSearchString(
        String jiraProjectKey,
        LinkableItem provider,
        LinkableItem project,
        LinkableItem projectVersion,
        LinkableItem component,
        @Nullable LinkableItem componentVersion,
        ComponentConcernType concernType,
        @Nullable String policyName
    ) {
        StringBuilder jqlBuilder = new StringBuilder();
        appendBlackDuckCommentSearchStrings(jqlBuilder, jiraProjectKey, project, projectVersion, component, componentVersion, concernType, policyName);
        jqlBuilder.append(" OR ");
        jqlBuilder.append("(");
        appendBlackDuckComponentSearchStrings(jqlBuilder, jiraProjectKey, provider, project, projectVersion, component, componentVersion);

        String category = JiraIssueSearchPropertyStringCompatibilityUtils.createCategory(concernType);
        appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY, category);

        if (ComponentConcernType.POLICY.equals(concernType) && null != policyName) {
            String additionalKey = JiraIssueSearchPropertyStringCompatibilityUtils.createPolicyAdditionalKey(policyName);
            appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_ADDITIONAL_KEY, additionalKey);
        }
        jqlBuilder.append(")");

        return jqlBuilder.toString();
    }

    // Helper methods

    private static void appendBlackDuckCommentSearchStrings(
        StringBuilder jqlBuilder,
        String jiraProjectKey,
        LinkableItem project,
        @Nullable LinkableItem projectVersion,
        @Nullable LinkableItem component,
        @Nullable LinkableItem componentVersion,
        @Nullable ComponentConcernType concernType,
        @Nullable String policyName) {

        jqlBuilder.append("(");
        appendProjectKey(jqlBuilder, jiraProjectKey);
        jqlBuilder.append(SEARCH_CONJUNCTION);
        jqlBuilder.append(StringUtils.SPACE);
        jqlBuilder.append(String.format("comment ~\"%s\"", JiraIssuePropertyKeys.JIRA_ISSUE_KEY_START_HEADER));
        jqlBuilder.append(StringUtils.SPACE);

        if(project != null && project.getUrl().isPresent()) {
            String projectId = extractUuid(project.getUrl().get(), "/api/projects/");
            appendCommentSearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_KEY_PROJECT_ID, projectId);
        }

        if(projectVersion != null && projectVersion.getUrl().isPresent()) {
            String projectVersionId = extractUuid(projectVersion.getUrl().get(), "/versions/");
            appendCommentSearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_KEY_PROJECT_VERSION_ID, projectVersionId);
        }

        if(component != null) {
            appendCommentSearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_KEY_COMPONENT_NAME, component.getValue());
        }

        if(componentVersion != null) {
            appendCommentSearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_KEY_COMPONENT_VERSION_NAME, componentVersion.getValue());
        }

        if(concernType != null) {
            appendCommentSearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_KEY_CATEGORY, concernType.name());
        }

        if(StringUtils.isNotBlank(policyName)) {
            String escapedPolicyName = JiraIssueSearchPropertyStringCompatibilityUtils.createPolicyAdditionalKey(policyName);
            appendCommentSearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_KEY_POLICY_NAME, escapedPolicyName);
        }
        jqlBuilder.append(")");
    }

    private static @NotNull String extractUuid(String url, String pathSearchToken) {
        int searchTokenStart = StringUtils.indexOf(url, pathSearchToken);
        int startIndex = searchTokenStart + pathSearchToken.length();
        int endSlashIndex = StringUtils.indexOf(url, '/', startIndex);
        String uuid;
        if(endSlashIndex > startIndex) {
            uuid = StringUtils.substring(url, startIndex, endSlashIndex);
        } else {
            uuid = StringUtils.substring(url, startIndex);
        }

        return uuid;
    }


    private static void appendBlackDuckComponentSearchStrings(
        StringBuilder jqlBuilder,
        String jiraProjectKey,
        LinkableItem provider,
        LinkableItem project,
        LinkableItem projectVersion,
        LinkableItem component,
        @Nullable LinkableItem componentVersion
    ) {
        appendBlackDuckProjectVersionSearchStrings(jqlBuilder, jiraProjectKey, provider, project, projectVersion);

        appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_LABEL, component.getLabel());
        appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_VALUE, component.getValue());

        if (null != componentVersion) {
            appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_NAME, componentVersion.getLabel());
            appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_VALUE, componentVersion.getValue());
        }
    }

    private static void appendBlackDuckProjectVersionSearchStrings(
        StringBuilder jqlBuilder,
        String jiraProjectKey,
        LinkableItem provider,
        LinkableItem project,
        LinkableItem projectVersion
    ) {
        appendBlackDuckProjectSearchStrings(jqlBuilder, jiraProjectKey, provider, project);

        appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_VERSION_LABEL, projectVersion.getLabel());
        appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_VERSION_NAME, projectVersion.getValue());
    }

    private static void appendBlackDuckProjectSearchStrings(StringBuilder jqlBuilder, String jiraProjectKey, LinkableItem provider, LinkableItem project) {
        appendProjectKey(jqlBuilder, jiraProjectKey);
        appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER, provider.getLabel());
        provider.getUrl()
            .flatMap(JiraIssueAlertPropertiesUrlCorrector::correctUrl)
            .ifPresent(url -> appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER_URL, url));
        appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_LABEL, project.getLabel());
        appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_NAME, project.getValue());
    }

    private static void appendProjectKey(StringBuilder jqlBuilder, String jiraProjectKey) {
        jqlBuilder.append(JiraConstants.JIRA_SEARCH_KEY_JIRA_PROJECT);
        jqlBuilder.append(" = '");
        jqlBuilder.append(escapeSearchString(jiraProjectKey));
        jqlBuilder.append("' ");
    }

    private static void appendCommentSearchString(StringBuilder jqlBuilder, String key, String value) {
        jqlBuilder.append(SEARCH_CONJUNCTION);
        jqlBuilder.append(" comment ~ \"");
        jqlBuilder.append(String.format("%s%s%s", key,JiraIssuePropertyKeys.JIRA_ISSUE_KEY_SEPARATOR, escapeSearchString(value)));
        jqlBuilder.append("\"");
        jqlBuilder.append(StringUtils.SPACE);
    }

    private static void appendPropertySearchString(StringBuilder jqlBuilder, String key, String value) {
        jqlBuilder.append(SEARCH_CONJUNCTION);
        jqlBuilder.append(StringUtils.SPACE);
        jqlBuilder.append(createPropertySearchString(key, value));
        jqlBuilder.append(StringUtils.SPACE);
    }

    private static String createPropertySearchString(String key, String value) {
        // To support backwards compatability of Jira tickets prior to Alert 8.0.0 we check if properties contain either the new blackduck (post 8.0.0)
        //  OR the old synopsys (pre 8.0.0) property index.
        //  Ex. "(issue.property[com-blackduck-integration-alert].provider = 'Black Duck' OR issue.property[com-synopsys-integration-alert].provider = 'Black Duck')"
        String propertySearchFormat = "(issue.property[%s].%s = '%s' OR issue.property[%s].%s = '%s')";
        String escapedValue = escapeSearchString(value);
        return String.format(propertySearchFormat, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, key, escapedValue, JiraConstants.JIRA_ISSUE_PROPERTY_OLD_KEY, key, escapedValue);
    }

    private static String escapeSearchString(String originalString) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Character character : originalString.toCharArray()) {
            // because the properties are now strings instead of text fields, we no longer need to escape the reserved characters
            // https://confluence.atlassian.com/jirasoftwarecloud/advanced-searching-764478330.html
            // we still need to escape single quotes in the string
            if (CHARACTERS_TO_ESCAPE.contains(character)) {
                stringBuilder.append('\\');
            }
            stringBuilder.append(character);
        }
        String escapedString = stringBuilder.toString();
        // if the string ends with a single backslash, we need to escape the single backslash
        if (escapedString.endsWith("\\") && !escapedString.endsWith("\\\\")) {
            stringBuilder.append('\\');
        }
        return stringBuilder.toString();
    }

    private JqlStringCreator() {
    }

}
