package com.synopsys.integration.alert.common.rest.model;

import java.util.List;

import org.springframework.data.domain.Page;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

public class JobPagedModel extends AlertPagedModel<JobFieldModel> {
    public JobPagedModel(int totalPages, int currentPage, int pageSize, List<JobFieldModel> models) {
        super(totalPages, currentPage, pageSize, models);
    }

    public List<JobFieldModel> getJobs() {
        return getModels();
    }

}
