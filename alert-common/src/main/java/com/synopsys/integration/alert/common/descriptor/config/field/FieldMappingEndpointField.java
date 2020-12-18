package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class FieldMappingEndpointField extends ConfigField {
    private String leftSide;
    private String rightSide;
    private String newMappingTitle;

    public FieldMappingEndpointField(String key, String label, String description, String leftSide, String rightSide) {
        super(key, label, description, FieldType.FIELD_MAPPING_INPUT);
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public FieldMappingEndpointField applyNewMappingTitle(String title) {
        this.newMappingTitle = title;
        return this;
    }

    public String getLeftSide() {
        return leftSide;
    }

    public String getRightSide() {
        return rightSide;
    }

    public String getNewMappingTitle() {
        return newMappingTitle;
    }
}
