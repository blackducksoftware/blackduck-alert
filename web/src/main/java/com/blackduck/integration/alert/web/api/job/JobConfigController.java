/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.job;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.BaseJobResourceController;
import com.blackduck.integration.alert.common.rest.api.ReadPageController;
import com.blackduck.integration.alert.common.rest.api.TestController;
import com.blackduck.integration.alert.common.rest.api.ValidateController;
import com.blackduck.integration.alert.common.rest.model.JobFieldModel;
import com.blackduck.integration.alert.common.rest.model.JobFieldStatuses;
import com.blackduck.integration.alert.common.rest.model.JobIdsRequestModel;
import com.blackduck.integration.alert.common.rest.model.JobPagedModel;

/**
 * @deprecated Deprecated in 8.x, planned for removed in 10.0.0.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping(JobConfigController.JOB_CONFIGURATION_PATH)
public class JobConfigController implements BaseJobResourceController, ReadPageController<JobPagedModel>, TestController<JobFieldModel>, ValidateController<JobFieldModel> {
    public static final String JOB_CONFIGURATION_PATH = AlertRestConstants.CONFIGURATION_PATH + "/job";
    private final JobConfigActions jobConfigActions;

    @Autowired
    public JobConfigController(JobConfigActions jobConfigActions) {
        this.jobConfigActions = jobConfigActions;
    }

    @PostMapping("/validateJobsById")
    public List<JobFieldStatuses> getValidationResultsForJobs(@RequestBody JobIdsRequestModel validationModel) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.validateJobsById(validationModel));
    }

    // This will check if the specified descriptor has a global config associated with it
    @PostMapping("/descriptorCheck")
    public String descriptorCheck(@RequestBody String descriptorName) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.checkGlobalConfigExists(descriptorName));
    }

    @Override
    public JobPagedModel getPage(Integer pageNumber, Integer pageSize, String searchTerm, String sortName, String sortOrder) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.getPage(pageNumber, pageSize, searchTerm, sortName, sortOrder));
    }

    @Override
    public JobFieldModel getOne(UUID id) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.getOne(id));
    }

    @Override
    public JobFieldModel create(JobFieldModel resource) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.create(resource));
    }

    @Override
    public void update(UUID id, JobFieldModel resource) {
        ResponseFactory.createContentResponseFromAction(jobConfigActions.update(id, resource));
    }

    @Override
    public void delete(UUID id) {
        ResponseFactory.createContentResponseFromAction(jobConfigActions.delete(id));
    }

    @Override
    public ValidationResponseModel validate(JobFieldModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.validate(requestBody));
    }

    @Override
    public ValidationResponseModel test(JobFieldModel resource) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.test(resource));
    }

}
