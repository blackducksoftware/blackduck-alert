package com.blackduck.integration.alert.common.rest.model;

import java.util.List;

public class MultiJobFieldModel extends MultiResponseModel<JobFieldModel> {
    public MultiJobFieldModel(final List<JobFieldModel> models) {
        super(models);
    }

    public List<JobFieldModel> getJobs() {
        return getModels();
    }

}
