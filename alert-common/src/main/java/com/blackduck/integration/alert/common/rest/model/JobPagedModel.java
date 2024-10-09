package com.blackduck.integration.alert.common.rest.model;

import java.util.List;

public class JobPagedModel extends AlertPagedModel<JobFieldModel> {
    public JobPagedModel(int totalPages, int currentPage, int pageSize, List<JobFieldModel> models) {
        super(totalPages, currentPage, pageSize, models);
    }

    public List<JobFieldModel> getJobs() {
        return getModels();
    }

}
