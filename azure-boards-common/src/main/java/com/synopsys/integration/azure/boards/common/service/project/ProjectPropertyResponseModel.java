package com.synopsys.integration.azure.boards.common.service.project;

import com.google.gson.JsonObject;

public class ProjectPropertyResponseModel {
    public static final String COMMON_PROPERTIES_PROCESS_ID = "System.ProcessTemplateType";

    private String name;
    private JsonObject value;

    public ProjectPropertyResponseModel() {
        // For serialization
    }

    public ProjectPropertyResponseModel(String name, JsonObject value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public JsonObject getValue() {
        return value;
    }

    public String getValueAsString() {
        return value.getAsJsonPrimitive().getAsString();
    }

    public boolean isValueAString() {
        return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
    }

}
