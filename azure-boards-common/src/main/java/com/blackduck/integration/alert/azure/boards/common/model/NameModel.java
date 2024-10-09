package com.blackduck.integration.alert.azure.boards.common.model;

public class NameModel {
    private String name;

    public NameModel() {
        // For serialization
    }

    public NameModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
