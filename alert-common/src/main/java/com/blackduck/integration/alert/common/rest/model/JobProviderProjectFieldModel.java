package com.blackduck.integration.alert.common.rest.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class JobProviderProjectFieldModel extends AlertSerializableModel {
    private String name;
    private String href;
    private Boolean missing;

    public JobProviderProjectFieldModel() {
    }

    public JobProviderProjectFieldModel(String name, String href, Boolean missing) {
        this.name = name;
        this.href = href;
        this.missing = missing;
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public Boolean getMissing() {
        return missing;
    }

    public void setMissing(Boolean missing) {
        this.missing = missing;
    }

}
