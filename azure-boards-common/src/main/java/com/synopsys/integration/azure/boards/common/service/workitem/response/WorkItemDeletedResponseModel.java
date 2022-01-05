/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
