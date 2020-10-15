package com.synopsys.integration.alert.common.rest.model;

import java.util.List;

import org.springframework.data.domain.Page;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

public class JobPagedModel extends AlertPagedModel<JobFieldModel> {
    public JobPagedModel(Page<?> page, List<JobFieldModel> jobs) {
        super(page, jobs);
    }

    public List<JobFieldModel> getJobs() {
        return getModels();
    }

}
