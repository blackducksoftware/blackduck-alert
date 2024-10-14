/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
