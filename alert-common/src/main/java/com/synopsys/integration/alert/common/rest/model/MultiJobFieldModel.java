/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.List;

public class MultiJobFieldModel extends MultiResponseModel<JobFieldModel> {
    public MultiJobFieldModel(final List<JobFieldModel> models) {
        super(models);
    }

    public List<JobFieldModel> getJobs() {
        return getModels();
    }

}
