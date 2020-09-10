/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.api.job;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseJobResourceController;
import com.synopsys.integration.alert.common.rest.api.ReadAllController;
import com.synopsys.integration.alert.common.rest.api.TestController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldStatuses;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.web.api.config.ConfigController;

@RestController
@RequestMapping(JobConfigController.JOB_CONFIGURATION_PATH)
public class JobConfigController implements BaseJobResourceController, ReadAllController, TestController<JobFieldModel>, ValidateController<JobFieldModel> {
    public static final String JOB_CONFIGURATION_PATH = ConfigController.CONFIGURATION_PATH + "/job";
    private final JobConfigActions jobConfigActions;

    @Autowired
    public JobConfigController(JobConfigActions jobConfigActions) {
        this.jobConfigActions = jobConfigActions;
    }

    @GetMapping("/validate")
    public List<JobFieldStatuses> validateJobs() {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.validateAllJobs());
    }

    // This will check if the specified descriptor has a global config associated with it
    @PostMapping("/descriptorCheck")
    public String descriptorCheck(@RequestBody String descriptorName) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.checkGlobalConfigExists(descriptorName));
    }

    @Override
    public List getAll() {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.getAll());
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
        ValidationActionResponse response = jobConfigActions.validate(requestBody);
        return ResponseFactory.createContentResponseFromAction(new ValidationActionResponse(HttpStatus.OK, response.getContent().orElse(null)));
    }

    @Override
    public ValidationResponseModel test(JobFieldModel resource) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.test(resource));
    }
}
