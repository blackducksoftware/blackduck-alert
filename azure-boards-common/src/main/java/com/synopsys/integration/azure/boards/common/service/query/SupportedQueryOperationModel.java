package com.synopsys.integration.azure.boards.common.service.query;

public class SupportedQueryOperationModel {
    private String name;
    private String referenceName;

    public SupportedQueryOperationModel() {
        // For serialization
    }

    public SupportedQueryOperationModel(String name, String referenceName) {
        this.name = name;
        this.referenceName = referenceName;
    }

    public String getName() {
        return name;
    }

    public String getReferenceName() {
        return referenceName;
    }

}
