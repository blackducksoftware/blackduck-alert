package com.synopsys.integration.alert.common.rest.model;

public class JobProviderProjectFieldModel extends AlertSerializableModel {
    private String name;
    private String href;
    private String projectOwnerEmail;

    public JobProviderProjectFieldModel() {
    }

    public JobProviderProjectFieldModel(String name, String href, String projectOwnerEmail) {
        this.name = name;
        this.href = href;
        this.projectOwnerEmail = projectOwnerEmail;
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public String getProjectOwnerEmail() {
        return projectOwnerEmail;
    }

}
