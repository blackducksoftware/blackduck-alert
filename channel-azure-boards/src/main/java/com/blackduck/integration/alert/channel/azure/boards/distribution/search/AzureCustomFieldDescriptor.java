package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

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
