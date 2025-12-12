/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira;

import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssuePropertyKeys;

public final class JiraConstants {
    public static final String DEFAULT_ISSUE_TYPE = "Task";
    // This String must always match the String found in the atlassian-connect.json file under key.
    public static final String JIRA_APP_KEY = "com.blackduck.integration.alert";
    public static final String JIRA_ALERT_APP_NAME = "Alert Issue Property Indexer";
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.key.
    public static final String JIRA_ISSUE_PROPERTY_KEY = "com-blackduck-integration-alert";
    // This String is used to support backwards compatability prior to rebranding in 8.0.0.
    public static final String JIRA_ISSUE_PROPERTY_OLD_KEY = "com-synopsys-integration-alert";

    public static final String JIRA_SEARCH_KEY_JIRA_PROJECT = "project";

    public static String createCommentMigrationJQL() {
        // find tickets created by alert first:
        // 1. A summary that starts with "Alert - Black Duck"
        // 2. A summary that isn't an Alert test message
        // 3. Then check if the new property key exists on that issue
        // 4. Then check if the new property alert9Migrated is empty or has the text "true".

         return "(summary ~ \"Alert - Black Duck\" OR summary !~ \"Alert Test Message\")"
                 + " AND "
                 + "(issue.property["
                 + JiraConstants.JIRA_ISSUE_PROPERTY_KEY
                 + "].topicName IS NOT EMPTY "
                 + "AND "
                 + "(issue.property["
                 + JiraConstants.JIRA_ISSUE_PROPERTY_KEY
                 + "].alert9Migrated IS EMPTY "
                 + "OR issue.property["
                 + JiraConstants.JIRA_ISSUE_PROPERTY_KEY
                 + "].alert9Migrated != 'true'"
                 + "))";
    }

    private JiraConstants() {
    }

}
