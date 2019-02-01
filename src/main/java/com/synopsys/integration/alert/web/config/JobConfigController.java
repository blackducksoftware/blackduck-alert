/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.web.config;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.controller.ResponseFactory;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.configuration.JobFieldModel;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@RestController
@RequestMapping(JobConfigController.JOB_CONFIGURATION_PATH)
public class JobConfigController extends BaseController {
    public static final String JOB_CONFIGURATION_PATH = ConfigController.CONFIGURATION_PATH + "/job";

    private final Logger logger = LoggerFactory.getLogger(JobConfigController.class);

    private JobConfigActions jobConfigActions;
    private ResponseFactory responseFactory;
    private ContentConverter contentConverter;

    @Autowired
    public JobConfigController(final JobConfigActions jobConfigActions, final ResponseFactory responseFactory, final ContentConverter contentConverter) {
        this.jobConfigActions = jobConfigActions;
        this.responseFactory = responseFactory;
        this.contentConverter = contentConverter;
    }

    @GetMapping
    public ResponseEntity<String> getJobs() {
        List<JobFieldModel> models;
        try {
            models = jobConfigActions.getAllJobs();
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "There was an issue retrieving data from the database.");
        }

        if (models.isEmpty()) {
            return responseFactory.createNotFoundResponse("Configurations not found for the context and descriptor provided");
        }

        return responseFactory.createOkContentResponse(contentConverter.getJsonString(models));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getJob(@PathVariable UUID id) {
        Optional<JobFieldModel> optionalModel;
        try {
            optionalModel = jobConfigActions.getJobById(id);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "There was an issue retrieving data from the database for ID: " + id);
        }

        if (optionalModel.isPresent()) {
            return responseFactory.createOkResponse(contentConverter.getStringValue(id), contentConverter.getJsonString(optionalModel.get()));
        }

        return responseFactory.createNotFoundResponse("Configuration not found for the specified id");
    }

    @PostMapping
    public ResponseEntity<String> postConfig(@RequestBody(required = true) final JobFieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", "Required request body is missing");
        }
        String id = restModel.getJobId();
        UUID uuid = UUID.fromString(id);
        try {
            if (!jobConfigActions.doesJobExist(uuid)) {
                try {
                    final JobFieldModel updatedEntity = jobConfigActions.saveJob(restModel);
                    return responseFactory.createCreatedResponse(updatedEntity.getJobId(), "Created");
                } catch (final AlertFieldException e) {
                    return responseFactory.createFieldErrorResponse(id, "There were errors with the configuration.", e.getFieldErrors());
                }
            } else {
                return responseFactory.createConflictResponse(id, "Provided id must not be in use. To update an existing configuration, use PUT.");
            }
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(restModel.getJobId(), e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> putConfig(@PathVariable final UUID id, @RequestBody(required = true) final JobFieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", "Required request body is missing");
        }

        String stringId = restModel.getJobId();
        try {
            if (jobConfigActions.doesJobExist(id)) {
                try {
                    final JobFieldModel updatedEntity = jobConfigActions.updateJob(id, restModel);
                    return responseFactory.createAcceptedResponse(updatedEntity.getJobId(), "Updated");
                } catch (final AlertFieldException e) {
                    return responseFactory.createFieldErrorResponse(stringId, "There were errors with the configuration.", e.getFieldErrors());
                }
            } else {
                return responseFactory.createBadRequestResponse(stringId, "No configuration with the specified id.");
            }
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(stringId, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConfig(@PathVariable final UUID id) {
        if (null == id) {
            responseFactory.createBadRequestResponse("", "Proper ID is required for deleting.");
        }
        String stringId = contentConverter.getStringValue(id);
        try {
            if (jobConfigActions.doesJobExist(id)) {
                jobConfigActions.deleteJobById(id);
                return responseFactory.createAcceptedResponse(stringId, "Deleted");
            } else {
                return responseFactory.createBadRequestResponse(stringId, "No configuration with the specified id.");
            }
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(stringId, e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = true) final JobFieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", "Required request body is missing");
        }
        String id = restModel.getJobId();
        try {
            final String responseMessage = jobConfigActions.validateJob(restModel);
            return responseFactory.createOkResponse(id, responseMessage);
        } catch (final AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, e.getMessage(), e.getFieldErrors());
        }
    }

    @PostMapping("/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = true) final JobFieldModel restModel, @RequestParam(required = false) final String destination) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", "Required request body is missing");
        }
        String id = restModel.getJobId();
        try {
            final String responseMessage = jobConfigActions.testJob(restModel, destination);
            return responseFactory.createOkResponse(id, responseMessage);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createMessageResponse(HttpStatus.valueOf(e.getHttpStatusCode()), id, e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, e.getMessage(), e.getFieldErrors());
        } catch (AlertMethodNotAllowedException e) {
            return responseFactory.createMethodNotAllowedResponse(e.getMessage());
        } catch (final AlertException e) {
            return responseFactory.createBadRequestResponse(id, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(id, e.getMessage());
        }
    }
}
