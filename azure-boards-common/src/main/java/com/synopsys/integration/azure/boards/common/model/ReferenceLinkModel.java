package com.synopsys.integration.azure.boards.common.model;

public class ReferenceLinkModel {
    private String href;

    public ReferenceLinkModel() {
        // For serialization
    }

    public ReferenceLinkModel(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

}
