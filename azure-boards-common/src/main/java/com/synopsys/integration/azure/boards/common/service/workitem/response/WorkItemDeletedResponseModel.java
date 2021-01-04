/**
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.azure.boards.common.service.workitem.response;

public class WorkItemDeletedResponseModel {
    private Integer code;
    private String deletedBy;
    private String deletedDate;
    private Integer id;
    private String message;
    private String name;
    private String project;
    private WorkItemResponseModel resource;
    private String type;
    private String url;

    public WorkItemDeletedResponseModel() {
        // For serialization
    }

    public Integer getCode() {
        return code;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public String getDeletedDate() {
        return deletedDate;
    }

    public Integer getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getProject() {
        return project;
    }

    public WorkItemResponseModel getResource() {
        return resource;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

}
