/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.board;

import com.blackduck.integration.alert.azure.boards.common.model.FieldReferenceModel;

public class BoardFieldsModel {
    private FieldReferenceModel columnField;
    private FieldReferenceModel doneField;
    private FieldReferenceModel rowField;

    public BoardFieldsModel() {
        // For serialization
    }

    public BoardFieldsModel(FieldReferenceModel columnField, FieldReferenceModel doneField, FieldReferenceModel rowField) {
        this.columnField = columnField;
        this.doneField = doneField;
        this.rowField = rowField;
    }

    public FieldReferenceModel getColumnField() {
        return columnField;
    }

    public FieldReferenceModel getDoneField() {
        return doneField;
    }

    public FieldReferenceModel getRowField() {
        return rowField;
    }

}
