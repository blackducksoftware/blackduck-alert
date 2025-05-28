/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.custom;

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
        assertFalse(jiraCustomFieldConfig.isTreatValueAsJson());
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

    @Test
    public void treatValueAsJsonTest() {
        JiraCustomFieldConfig jiraCustomFieldConfig = new JiraCustomFieldConfig(fieldName, fieldOriginalValue, true);
        assertTrue(jiraCustomFieldConfig.isTreatValueAsJson());
    }
}
