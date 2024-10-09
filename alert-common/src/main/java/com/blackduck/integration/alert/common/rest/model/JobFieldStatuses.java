package com.blackduck.integration.alert.common.rest.model;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;

public class JobFieldStatuses extends Config {
    private final List<AlertFieldStatus> fieldStatuses;

    public JobFieldStatuses(List<AlertFieldStatus> fieldStatuses) {
        this.fieldStatuses = fieldStatuses;
    }

    public JobFieldStatuses(String id, List<AlertFieldStatus> fieldStatuses) {
        super(id);
        this.fieldStatuses = fieldStatuses;
    }

    public List<AlertFieldStatus> getFieldStatuses() {
        return fieldStatuses;
    }

}
