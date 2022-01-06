/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

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
