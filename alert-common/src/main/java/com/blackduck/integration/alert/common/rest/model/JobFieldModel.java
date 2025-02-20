/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.model;

import java.util.List;
import java.util.Set;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class JobFieldModel extends AlertSerializableModel {
    private String jobId;
    private Set<FieldModel> fieldModels;
    private List<JobProviderProjectFieldModel> configuredProviderProjects;

    public JobFieldModel() {
        this(null, null, null);
    }

    public JobFieldModel(String jobId, Set<FieldModel> fieldModels, List<JobProviderProjectFieldModel> configuredProviderProjects) {
        this.jobId = jobId;
        this.fieldModels = fieldModels;
        this.configuredProviderProjects = configuredProviderProjects;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Set<FieldModel> getFieldModels() {
        return fieldModels;
    }

    public void setFieldModels(Set<FieldModel> fieldModels) {
        this.fieldModels = fieldModels;
    }

    public List<JobProviderProjectFieldModel> getConfiguredProviderProjects() {
        return configuredProviderProjects;
    }

    public void setConfiguredProviderProjects(List<JobProviderProjectFieldModel> configuredProviderProjects) {
        this.configuredProviderProjects = configuredProviderProjects;
    }

}
