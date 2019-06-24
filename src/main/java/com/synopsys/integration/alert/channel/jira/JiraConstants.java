package com.synopsys.integration.alert.channel.jira;

public class JiraConstants {
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.key.
    public static final String JIRA_ISSUE_PROPERTY_KEY = "com-synopsys-integration-alert";
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-synopsys-integration-alert"].extractions.objectName.
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_BOM_COMPONENT_URI = "bomComponentUri";
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-synopsys-integration-alert"].extractions.objectName.
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY = "category";
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-synopsys-integration-alert"].extractions.objectName.
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_POLICY_NAME = "policyName";

}
