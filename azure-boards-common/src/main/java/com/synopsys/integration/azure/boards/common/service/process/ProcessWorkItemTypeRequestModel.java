/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.process;

public class ProcessWorkItemTypeRequestModel {
    private String inheritsFrom;
    private String name;
    private String description;
    private String icon;
    private String color;
    private Boolean isDisabled;

    public static ProcessWorkItemTypeRequestModel copyWorkItem(ProcessWorkItemTypesResponseModel processWorkItemTypesResponseModel) {
        String inheritsFrom = processWorkItemTypesResponseModel.getReferenceName();
        String name = processWorkItemTypesResponseModel.getName();
        String description = processWorkItemTypesResponseModel.getDescription();
        String icon = processWorkItemTypesResponseModel.getIcon();
        String color = processWorkItemTypesResponseModel.getColor();
        Boolean isDisabled = processWorkItemTypesResponseModel.getDisabled();
        return new ProcessWorkItemTypeRequestModel(inheritsFrom, name, description, icon, color, isDisabled);
    }

    public ProcessWorkItemTypeRequestModel() {
        // For serialization
    }

    public ProcessWorkItemTypeRequestModel(String inheritsFrom, String name, String description, String icon, String color, Boolean isDisabled) {
        this.inheritsFrom = inheritsFrom;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.color = color;
        this.isDisabled = isDisabled;
    }

    public String getInheritsFrom() {
        return inheritsFrom;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public Boolean getDisabled() {
        return isDisabled;
    }
}
