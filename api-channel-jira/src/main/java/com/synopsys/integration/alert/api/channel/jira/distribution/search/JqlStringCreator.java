/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

public final class JqlStringCreator {
    private static final String SEARCH_CONJUNCTION = "AND";
    private static final Set<Character> CHARACTERS_TO_ESCAPE = Set.of('\'');

    public static String createBlackDuckProjectIssuesSearchString(
        String jiraProjectKey,
        LinkableItem provider,
        LinkableItem project
    ) {
        StringBuilder jqlBuilder = new StringBuilder();
        appendBlackDuckProjectSearchStrings(jqlBuilder, jiraProjectKey, provider, project);
        return jqlBuilder.toString();
    }

    public static String createBlackDuckProjectVersionIssuesSearchString(
        String jiraProjectKey,
        LinkableItem provider,
        LinkableItem project,
        LinkableItem projectVersion
    ) {
        StringBuilder jqlBuilder = new StringBuilder();
        appendBlackDuckProjectVersionSearchStrings(jqlBuilder, jiraProjectKey, provider, project, projectVersion);

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
        appendBlackDuckComponentSearchStrings(jqlBuilder, jiraProjectKey, provider, project, projectVersion, component, componentVersion);

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
        appendBlackDuckComponentSearchStrings(jqlBuilder, jiraProjectKey, provider, project, projectVersion, component, componentVersion);

        String category = JiraIssueSearchPropertyStringCompatibilityUtils.createCategory(concernType);
        appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY, category);

        if (ComponentConcernType.POLICY.equals(concernType) && null != policyName) {
            String additionalKey = JiraIssueSearchPropertyStringCompatibilityUtils.createPolicyAdditionalKey(policyName);
            appendPropertySearchString(jqlBuilder, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_ADDITIONAL_KEY, additionalKey);
        }

        return jqlBuilder.toString();
    }

    // Helper methods

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

    private static void appendBlackDuckProjectVersionSearchStrings(StringBuilder jqlBuilder, String jiraProjectKey, LinkableItem provider, LinkableItem project, LinkableItem projectVersion) {
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

    private static void appendPropertySearchString(StringBuilder jqlBuilder, String key, String value) {
        jqlBuilder.append(SEARCH_CONJUNCTION);
        jqlBuilder.append(StringUtils.SPACE);
        jqlBuilder.append(createPropertySearchString(key, value));
        jqlBuilder.append(StringUtils.SPACE);
    }

    private static String createPropertySearchString(String key, String value) {
        String propertySearchFormat = "issue.property[%s].%s = '%s'";
        String escapedValue = escapeSearchString(value);
        return String.format(propertySearchFormat, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, key, escapedValue);
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
