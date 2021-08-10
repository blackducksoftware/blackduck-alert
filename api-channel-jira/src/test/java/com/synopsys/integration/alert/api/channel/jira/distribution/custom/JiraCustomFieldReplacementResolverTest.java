package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

//TODO: Move this test for MessageReplacementResolver and remove Jira references
public class JiraCustomFieldReplacementResolverTest {
    @Test
    public void replacementFieldValueTest() {
        String originalFieldValue = "ProjectName: {{projectName}} | ProviderName: {{providerName}} | ProjectVersionName: {{projectVersion}}";
        String expectedString = "ProjectName: ProjectNameREPLACED | ProviderName: ProviderNameREPLACED | ProjectVersionName: ProjectVersionNameREPLACED";

        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .projectVersionName("ProjectVersionNameREPLACED")
                                                                .build();

        testReplacements(jiraCustomFieldConfig, messageReplacementValues, expectedString);
    }

    @Test
    public void replacementFieldValueNullTest() {
        String originalFieldValue = "ProjectName: {{projectName}}, ProjectVersion: {{projectVersion}}, ComponentName: {{componentName}}, ComponentVersion: {{componentVersion}}";
        String expectedString = "ProjectName: ProjectNameREPLACED, ProjectVersion: None, ComponentName: None, ComponentVersion: None";

        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        testReplacements(jiraCustomFieldConfig, messageReplacementValues, expectedString);
    }

    @Test
    public void doubleReplacementTest() {
        String originalFieldValue = "ProjectName: {{projectName}} | ProjectName2: {{projectName}}";
        String expectedString = "ProjectName: ProjectNameREPLACED | ProjectName2: ProjectNameREPLACED";

        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        testReplacements(jiraCustomFieldConfig, messageReplacementValues, expectedString);
    }

    @Test
    public void badReplacementValueTest() {
        String originalFieldValue = "ProjectName: {{notAValidReplacement}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        testReplacements(jiraCustomFieldConfig, messageReplacementValues, originalFieldValue);
    }

    @Test
    public void missingBuilderReplacementTest() {
        String originalFieldValue = "Missing from builder: {{severity}}";
        String expectedFieldValue = "Missing from builder: None";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        testReplacements(jiraCustomFieldConfig, messageReplacementValues, expectedFieldValue);
    }

    @Test
    public void replacementSeverityTest() {
        String originalFieldValue = "Severity: {{severity}}";
        String expectedString = "Severity: CRITICAL";

        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .severity("CRITICAL")
                                                                .build();

        testReplacements(jiraCustomFieldConfig, messageReplacementValues, expectedString);
    }

    @Test
    public void replacementPolicyCategoryTest() {
        String originalFieldValue = "Policy Category: {{policyCategory}}";
        String expectedString = "Policy Category: UNCATEGORIZED";

        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .policyCategory("UNCATEGORIZED")
                                                                .build();

        testReplacements(jiraCustomFieldConfig, messageReplacementValues, expectedString);
    }

    @Test
    public void replacementComponentUsageTest() {
        String originalFieldValue = "Component Usage: {{componentUsage}}";
        String expectedString = "Component Usage: Dynamically Linked";

        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .componentUsage("Dynamically Linked")
                                                                .build();

        testReplacements(jiraCustomFieldConfig, messageReplacementValues, expectedString);
    }

    @Test
    public void replacementComponentLicenseTest() {
        String originalFieldValue = "Component License: {{componentLicense}}";
        String expectedString = "Component License: GPL-2.0";

        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .componentLicense("GPL-2.0")
                                                                .build();

        testReplacements(jiraCustomFieldConfig, messageReplacementValues, expectedString);
    }

    private void testReplacements(JiraCustomFieldConfig jiraCustomFieldConfig, MessageReplacementValues messageReplacementValues, String expectedString) {
        MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(messageReplacementValues);
        String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(jiraCustomFieldConfig.getFieldOriginalValue());
        jiraCustomFieldConfig.setFieldReplacementValue(replacedFieldValue);

        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }
}
