package com.synopsys.integration.distribution.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldConfig;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.MessageReplacementValues;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.MessageValueReplacementResolver;

public class JiraCustomFieldReplacementResolverTest {
    @Test
    public void replacementFieldValueTest() {
        String originalFieldValue = "ProjectName: {{projectName}} | ProviderName: {{providerName}} | ProjectVersionName: {{projectVersion}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .projectVersionName("ProjectVersionNameREPLACED")
                                                                .build();

        MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(messageReplacementValues);
        String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(jiraCustomFieldConfig.getFieldOriginalValue());
        jiraCustomFieldConfig.setFieldReplacementValue(replacedFieldValue);

        String expectedString = "ProjectName: ProjectNameREPLACED | ProviderName: ProviderNameREPLACED | ProjectVersionName: ProjectVersionNameREPLACED";
        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }

    @Test
    public void replacementFieldValueNullTest() {
        String originalFieldValue = "ProjectName: {{projectName}}, ProjectVersion: {{projectVersion}}, ComponentName: {{componentName}}, ComponentVersion: {{componentVersion}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(messageReplacementValues);
        String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(jiraCustomFieldConfig.getFieldOriginalValue());
        jiraCustomFieldConfig.setFieldReplacementValue(replacedFieldValue);

        String expectedString = "ProjectName: ProjectNameREPLACED, ProjectVersion: None, ComponentName: None, ComponentVersion: None";
        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }

    @Test
    public void doubleReplacementTest() {
        String originalFieldValue = "ProjectName: {{projectName}} | ProjectName2: {{projectName}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(messageReplacementValues);
        String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(jiraCustomFieldConfig.getFieldOriginalValue());
        jiraCustomFieldConfig.setFieldReplacementValue(replacedFieldValue);

        String expectedString = "ProjectName: ProjectNameREPLACED | ProjectName2: ProjectNameREPLACED";
        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }

    @Test
    public void noReplacementTest() {
        String originalFieldValue = "ProjectName: {{notAValidReplacement}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(messageReplacementValues);
        String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(jiraCustomFieldConfig.getFieldOriginalValue());
        jiraCustomFieldConfig.setFieldReplacementValue(replacedFieldValue);

        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(originalFieldValue, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }

    @Test
    public void replacementSeverityTest() {
        String originalFieldValue = "Severity: {{severity}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .projectVersionName("ProjectVersionNameREPLACED")
                                                                .severity("CRITICAL")
                                                                .build();

        MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(messageReplacementValues);
        String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(jiraCustomFieldConfig.getFieldOriginalValue());
        jiraCustomFieldConfig.setFieldReplacementValue(replacedFieldValue);

        String expectedString = "Severity: CRITICAL";
        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }

    @Test
    public void replacementPolicyCategoryTest() {
        String originalFieldValue = "Policy Category: {{policyCategory}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .projectVersionName("ProjectVersionNameREPLACED")
                                                                .policyCategory("UNCATEGORIZED")
                                                                .build();
        MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(messageReplacementValues);
        String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(jiraCustomFieldConfig.getFieldOriginalValue());
        jiraCustomFieldConfig.setFieldReplacementValue(replacedFieldValue);

        String expectedString = "Policy Category: UNCATEGORIZED";
        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }
}
