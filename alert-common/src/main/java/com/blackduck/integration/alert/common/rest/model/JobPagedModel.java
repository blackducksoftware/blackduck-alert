/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
