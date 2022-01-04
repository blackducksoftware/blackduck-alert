/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.workitem.request;

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
