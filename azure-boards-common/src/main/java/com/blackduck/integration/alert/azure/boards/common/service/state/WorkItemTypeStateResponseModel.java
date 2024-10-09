package com.blackduck.integration.alert.azure.boards.common.service.state;

public class WorkItemTypeStateResponseModel {
    private String name;
    private String color;
    private String category;

    public WorkItemTypeStateResponseModel() {
        // For serialization
    }

    public WorkItemTypeStateResponseModel(String name, String color, String category) {
        this.name = name;
        this.color = color;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getCategory() {
        return category;
    }

}
