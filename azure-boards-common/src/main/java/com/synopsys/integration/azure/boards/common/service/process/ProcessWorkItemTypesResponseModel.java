/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.process;

public class ProcessWorkItemTypesResponseModel {
    private String referenceName;
    private String name;
    private String description;
    private String url;
    private String customization;
    private String color;
    private String icon;
    private Boolean isDisabled;
    private String inherits;

    public ProcessWorkItemTypesResponseModel() {
        // For serialization
    }

    public ProcessWorkItemTypesResponseModel(String referenceName, String name, String description, String url, String customization, String color, String icon, Boolean isDisabled, String inherits) {
        this.referenceName = referenceName;
        this.name = name;
        this.description = description;
        this.url = url;
        this.customization = customization;
        this.color = color;
        this.icon = icon;
        this.isDisabled = isDisabled;
        this.inherits = inherits;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getCustomization() {
        return customization;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public Boolean getDisabled() {
        return isDisabled;
    }

    public String getInherits() {
        return inherits;
    }

}
