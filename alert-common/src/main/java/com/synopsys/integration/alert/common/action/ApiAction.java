package com.synopsys.integration.alert.common.action;

import com.synopsys.integration.alert.common.rest.model.FieldModel;

public class ApiAction {

    public FieldModel beforeSaveAction(final FieldModel fieldModel) {
        return fieldModel;
    }

    public FieldModel afterSaveAction(final FieldModel fieldModel) {
        return fieldModel;
    }

    public FieldModel beforeUpdateAction(final FieldModel fieldModel) {
        return fieldModel;
    }

    public FieldModel afterUpdateAction(final FieldModel fieldModel) {
        return fieldModel;
    }

    public FieldModel beforeDeleteAction(final FieldModel fieldModel) {
        return fieldModel;
    }

    public void afterDeleteAction(final String descriptorName, final String context) {
    }

    public FieldModel afterGetAction(final FieldModel fieldModel) {
        return fieldModel;
    }

}
