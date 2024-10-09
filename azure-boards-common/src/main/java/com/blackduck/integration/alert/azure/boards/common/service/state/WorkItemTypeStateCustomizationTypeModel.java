package com.blackduck.integration.alert.azure.boards.common.service.state;

public class WorkItemTypeStateCustomizationTypeModel {
    private String custom;
    private String inherited;
    private String system;

    public WorkItemTypeStateCustomizationTypeModel() {
        // For serialization
    }

    public WorkItemTypeStateCustomizationTypeModel(String custom, String inherited, String system) {
        this.custom = custom;
        this.inherited = inherited;
        this.system = system;
    }

    public String getCustom() {
        return custom;
    }

    public String getInherited() {
        return inherited;
    }

    public String getSystem() {
        return system;
    }

}
