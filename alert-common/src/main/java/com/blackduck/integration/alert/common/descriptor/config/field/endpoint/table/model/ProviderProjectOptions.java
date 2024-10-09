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
