package com.synopsys.integration.azure.boards.common.service.process;

public class ProcessFieldResponseModel {
    private Boolean allowGroups;
    private String customization;
    private Object defaultValue;
    private String description;
    private String name;
    private Boolean readOnly;
    private String referenceName;
    private Boolean required;
    private String type;
    private String url;

    public ProcessFieldResponseModel() {
        // For serialization
    }

    public ProcessFieldResponseModel(Boolean allowGroups, String customization, Object defaultValue, String description, String name, Boolean readOnly, String referenceName, Boolean required, String type, String url) {
        this.allowGroups = allowGroups;
        this.customization = customization;
        this.defaultValue = defaultValue;
        this.description = description;
        this.name = name;
        this.readOnly = readOnly;
        this.referenceName = referenceName;
        this.required = required;
        this.type = type;
        this.url = url;
    }

    public Boolean getAllowGroups() {
        return allowGroups;
    }

    public String getCustomization() {
        return customization;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public Boolean getRequired() {
        return required;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

}
