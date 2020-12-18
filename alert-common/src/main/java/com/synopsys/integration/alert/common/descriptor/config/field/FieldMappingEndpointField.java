package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointField;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

public class FieldMappingEndpointField extends EndpointField {
    private String leftSide;
    private String rightSide;
    private String newMappingTitle;

    public FieldMappingEndpointField(String key, String label, String description, String leftSide, String rightSide) {
        super(key, label, description, FieldType.FIELD_MAPPING_INPUT, "Button Label", AbstractFunctionController.API_FUNCTION_URL);
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
