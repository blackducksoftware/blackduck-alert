/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

public class NamedDistributionJobDetailsModel<D extends DistributionJobDetailsModel> {
    private final D distributionJobDetailsModel;
    private final String jobName;

    public NamedDistributionJobDetailsModel(D distributionJobDetailsModel, String jobName) {
        this.distributionJobDetailsModel = distributionJobDetailsModel;
        this.jobName = jobName;
    }

    public D getDistributionJobDetailsModel() {
        return distributionJobDetailsModel;
    }

    public String getJobName() {
        return jobName;
    }
}
