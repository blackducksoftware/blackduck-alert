/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.config.field.endpoint.table.model;

import java.util.List;

import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

public class ProviderProjectOptions extends AlertPagedModel<ProviderProjectSelectOption> {
    public ProviderProjectOptions(int totalPages, int currentPage, int pageSize, List<ProviderProjectSelectOption> options) {
        super(totalPages, currentPage, pageSize, options);
    }

    public List<ProviderProjectSelectOption> getOptions() {
        return getModels();
    }

}
