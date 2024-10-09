package com.blackduck.integration.alert.common.rest.model;

import java.util.List;

public class MultiFieldModel extends MultiResponseModel<FieldModel> {
    MultiFieldModel() {
        // For serialization
        super();
    }

    public MultiFieldModel(List<FieldModel> models) {
        super(models);
    }

    public List<FieldModel> getFieldModels() {
        return getModels();
    }

}
