/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common.distribution.custom;

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
