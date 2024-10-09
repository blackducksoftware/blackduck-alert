package com.blackduck.integration.alert.api.channel.jira.distribution.custom;

public class JiraResolvedCustomField {
    private final String fieldId;
    private final Object fieldValue;

    public JiraResolvedCustomField(String fieldId, Object fieldValue) {
        this.fieldId = fieldId;
        this.fieldValue = fieldValue;
    }

    public String getFieldId() {
        return fieldId;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

}
