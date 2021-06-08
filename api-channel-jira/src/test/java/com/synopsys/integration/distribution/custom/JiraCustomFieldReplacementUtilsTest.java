package com.synopsys.integration.distribution.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldConfig;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldReplacementValues;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldValueReplacementUtils;

public class JiraCustomFieldReplacementUtilsTest {

    @Test
    public void replacementFieldValueTest() {
        //String originalFieldValue = "{{projectName}}";
        //String originalFieldValue = "pants";
        String originalFieldValue = "ProjectName: {{projectName}} | ProviderName: {{providerName}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        JiraCustomFieldReplacementValues jiraCustomFieldReplacementValues = new JiraCustomFieldReplacementValues("ProviderNameREPLACED", "ProjectNameREPLACED", null, null, null);

        JiraCustomFieldValueReplacementUtils.injectReplacementFieldValue(jiraCustomFieldConfig, jiraCustomFieldReplacementValues);

        String expectedString = "ProjectName: ProjectNameREPLACED | ProviderName: ProviderNameREPLACED";
        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }

    @Test
    public void replacementFieldValueProjectVersionTest() {
        String originalFieldValue = "ProjectVersion: {{projectVersion}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        JiraCustomFieldReplacementValues jiraCustomFieldReplacementValues = new JiraCustomFieldReplacementValues("ProviderNameREPLACED", "ProjectNameREPLACED", null, null, null);

        JiraCustomFieldValueReplacementUtils.injectReplacementFieldValue(jiraCustomFieldConfig, jiraCustomFieldReplacementValues);

        String expectedString = "ProjectVersion: None";
        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }

    @Test
    public void doubleReplacementTest() {
        String originalFieldValue = "ProjectName: {{projectName}} | ProjectName2: {{projectName}}";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig("testLabel", originalFieldValue);
        JiraCustomFieldReplacementValues jiraCustomFieldReplacementValues = new JiraCustomFieldReplacementValues("ProviderNameREPLACED", "ProjectNameREPLACED", null, null, null);

        JiraCustomFieldValueReplacementUtils.injectReplacementFieldValue(jiraCustomFieldConfig, jiraCustomFieldReplacementValues);

        String expectedString = "ProjectName: ProjectNameREPLACED | ProjectName2: ProjectNameREPLACED";
        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(expectedString, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }
}
