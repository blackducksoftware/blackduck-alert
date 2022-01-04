/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

public class AzureCustomFieldDescriptor {
    private final String fieldName;
    private final String fieldReferenceName;
    private final String fieldDescription;

    public AzureCustomFieldDescriptor(String fieldName, String fieldReferenceName, String fieldDescription) {
        this.fieldName = fieldName;
        this.fieldReferenceName = fieldReferenceName;
        this.fieldDescription = fieldDescription;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldReferenceName() {
        return fieldReferenceName;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }

}
