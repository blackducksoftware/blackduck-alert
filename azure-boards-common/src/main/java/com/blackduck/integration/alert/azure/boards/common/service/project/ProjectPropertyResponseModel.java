package com.blackduck.integration.alert.azure.boards.common.service.project;

public class ProjectPropertyResponseModel {
    public static final String COMMON_PROPERTIES_PROCESS_ID = "System.ProcessTemplateType";

    private String name;
    private String value;

    public ProjectPropertyResponseModel() {
        // For serialization
    }

    public ProjectPropertyResponseModel(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
