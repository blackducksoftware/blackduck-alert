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
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues("ProviderNameREPLACED", "ProjectNameREPLACED", "ProjectVersionNameREPLACED", null, null, null);

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
        MessageReplacementValues messageReplacementValues = MessageReplacementValues.trivial("ProviderNameREPLACED", "ProjectNameREPLACED");

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
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues("ProviderNameREPLACED", "ProjectNameREPLACED", null, null, null, null);

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
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues("ProviderNameREPLACED", "ProjectNameREPLACED", null, null, null, null);

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
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues("ProviderNameREPLACED", "ProjectNameREPLACED", "ProjectVersionNameREPLACED", null, null, "CRITICAL");

        MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(messageReplacementValues);
        String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(jiraCustomFieldConfig.getFieldOriginalValue());
        jiraCustomFieldConfig.setFieldReplacementValue(replacedFieldValue);

        String expectedString = "Severity: CRITICAL";
        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }
}
