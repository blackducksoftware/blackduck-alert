package com.synopsys.integration.azure.boards.common.service.board;

import com.synopsys.integration.azure.boards.common.model.FieldReferenceModel;

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
