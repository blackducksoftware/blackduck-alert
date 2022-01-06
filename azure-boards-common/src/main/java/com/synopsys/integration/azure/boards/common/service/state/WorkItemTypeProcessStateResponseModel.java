/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.state;

public class WorkItemTypeProcessStateResponseModel {
    private String id;
    private String name;
    private Integer order;
    private String stateCategory;
    private String color;
    private WorkItemTypeStateCustomizationTypeModel customizationType;
    private Boolean hidden;
    private String url;

    public WorkItemTypeProcessStateResponseModel() {
        // For serialization
    }

    public WorkItemTypeProcessStateResponseModel(String id, String name, Integer order, String stateCategory, String color, WorkItemTypeStateCustomizationTypeModel customizationType, Boolean hidden, String url) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.stateCategory = stateCategory;
        this.color = color;
        this.customizationType = customizationType;
        this.hidden = hidden;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

    public String getStateCategory() {
        return stateCategory;
    }

    public String getColor() {
        return color;
    }

    public WorkItemTypeStateCustomizationTypeModel getCustomizationType() {
        return customizationType;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public String getUrl() {
        return url;
    }

}
