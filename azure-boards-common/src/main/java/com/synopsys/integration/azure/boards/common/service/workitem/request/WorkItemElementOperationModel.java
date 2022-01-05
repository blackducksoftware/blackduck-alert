/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.workitem.request;

import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class WorkItemElementOperationModel {
    private String op;
    private String path;
    private String from;
    private Object value;

    public static WorkItemElementOperationModel fieldElement(WorkItemElementOperation operation, AzureFieldDefinition<?> fieldDefinition, Object value) {
        String fieldPath = String.format("/fields/%s", fieldDefinition.getFieldName());
        return new WorkItemElementOperationModel(operation.toLowerCaseString(), fieldPath, null, value);
    }

    public WorkItemElementOperationModel() {
        // For serialization
    }

    public WorkItemElementOperationModel(String op, String path, String from, Object value) {
        this.op = op;
        this.path = path;
        this.from = from;
        this.value = value;
    }

    public String getOp() {
        return op;
    }

    public String getPath() {
        return path;
    }

    public String getFrom() {
        return from;
    }

    public Object getValue() {
        return value;
    }

}
