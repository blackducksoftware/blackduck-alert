/**
 * azure-boards-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
