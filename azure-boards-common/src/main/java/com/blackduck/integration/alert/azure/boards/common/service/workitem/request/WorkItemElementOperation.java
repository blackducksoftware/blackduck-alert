package com.blackduck.integration.alert.azure.boards.common.service.workitem.request;

public enum WorkItemElementOperation {
    ADD,
    COPY,
    MOVE,
    REMOVE,
    REPLACE,
    TEST;

    public String toLowerCaseString() {
        return this.name().toLowerCase();
    }

}
