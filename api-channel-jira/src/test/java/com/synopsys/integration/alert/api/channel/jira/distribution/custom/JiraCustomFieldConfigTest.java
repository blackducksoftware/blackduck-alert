package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class JiraCustomFieldConfigTest {
    private final String fieldName = "field name";
    private final String fieldOriginalValue = "original value";

    @Test
    public void getFieldsTest() {
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig(fieldName, fieldOriginalValue);

        assertEquals(fieldName, jiraCustomFieldConfig.getFieldName());
        assertEquals(fieldOriginalValue, jiraCustomFieldConfig.getFieldOriginalValue());
    }

    @Test
    public void fieldReplacementValueTest() {
        String replacementValue = "replacementValue";
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig(fieldName, fieldOriginalValue);
        jiraCustomFieldConfig.setFieldReplacementValue(replacementValue);

        assertTrue(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
        assertEquals(replacementValue, jiraCustomFieldConfig.getFieldReplacementValue().get());
    }

    @Test
    public void fieldReplacementValueNullTest() {
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig(fieldName, fieldOriginalValue);
        jiraCustomFieldConfig.setFieldReplacementValue(null);

        assertFalse(jiraCustomFieldConfig.getFieldReplacementValue().isPresent());
    }
}
