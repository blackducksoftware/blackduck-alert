/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public class ProviderProjectOptions extends AlertPagedModel<ProviderProjectSelectOption> {
    public ProviderProjectOptions(int totalPages, int currentPage, int pageSize, List<ProviderProjectSelectOption> options) {
        super(totalPages, currentPage, pageSize, options);
    }

    public List<ProviderProjectSelectOption> getOptions() {
        return getModels();
    }

}
