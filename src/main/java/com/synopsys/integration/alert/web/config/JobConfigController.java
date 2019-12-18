/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingFunction;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@RestController
@RequestMapping(JobConfigController.JOB_CONFIGURATION_PATH)
public class JobConfigController extends BaseController {
    public static final String JOB_CONFIGURATION_PATH = ConfigController.CONFIGURATION_PATH + "/job";
    private static final EnumSet<DescriptorType> ALLOWED_JOB_DESCRIPTOR_TYPES = EnumSet.of(DescriptorType.PROVIDER, DescriptorType.CHANNEL);
    private final Logger logger = LoggerFactory.getLogger(JobConfigController.class);
    private final JobConfigActions jobConfigActions;
    private final ResponseFactory responseFactory;
    private final ContentConverter contentConverter;
    private final AuthorizationManager authorizationManager;
    private final DescriptorAccessor descriptorAccessor;
    private final DescriptorMap descriptorMap;
    private PKIXErrorResponseFactory pkixErrorResponseFactory;

    @Autowired
    public JobConfigController(JobConfigActions jobConfigActions, ResponseFactory responseFactory, ContentConverter contentConverter, AuthorizationManager authorizationManager,
        DescriptorAccessor descriptorAccessor, PKIXErrorResponseFactory pkixErrorResponseFactory, DescriptorMap descriptorMap) {
        this.jobConfigActions = jobConfigActions;
        this.responseFactory = responseFactory;
        this.contentConverter = contentConverter;
        this.authorizationManager = authorizationManager;
        this.descriptorAccessor = descriptorAccessor;
        this.pkixErrorResponseFactory = pkixErrorResponseFactory;
        this.descriptorMap = descriptorMap;
    }

    @GetMapping
    public ResponseEntity<String> getJobs() {
        try {
            Set<String> descriptorNames = descriptorAccessor.getRegisteredDescriptors()
                                              .stream()
                                              .filter(descriptor -> ALLOWED_JOB_DESCRIPTOR_TYPES.contains(descriptor.getType()))
                                              .map(RegisteredDescriptorModel::getName)
                                              .collect(Collectors.toSet());
            if (!authorizationManager.anyReadPermission(List.of(ConfigContextEnum.DISTRIBUTION.name()), descriptorNames)) {
                return responseFactory.createForbiddenResponse();
            }
            List<JobFieldModel> models = new LinkedList<>();
            List<JobFieldModel> allModels = jobConfigActions.getAllJobs();

            for (JobFieldModel jobModel : allModels) {
                boolean includeJob = hasRequiredPermissions(jobModel.getFieldModels(), authorizationManager::hasReadPermission);
                if (includeJob) {
                    models.add(jobModel);
                }
            }
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(models));
        } catch (AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "There was an issue retrieving data from the database.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getJob(@PathVariable UUID id) {
        Optional<JobFieldModel> optionalModel;
        try {
            optionalModel = jobConfigActions.getJobById(id);
        } catch (AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "There was an issue retrieving data from the database for ID: " + id);
        }

        if (optionalModel.isPresent()) {
            JobFieldModel fieldModel = optionalModel.get();
            boolean hasPermissions = hasRequiredPermissions(fieldModel.getFieldModels(), authorizationManager::hasReadPermission);
            if (!hasPermissions) {
                return responseFactory.createForbiddenResponse();
            }
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(fieldModel));
        }

        return responseFactory.createNotFoundResponse("Configuration not found for the specified id");
    }

    @PostMapping
    public ResponseEntity<String> postConfig(@RequestBody(required = true) JobFieldModel restModel) {
        boolean hasPermissions = hasRequiredPermissions(restModel.getFieldModels(), authorizationManager::hasCreatePermission);
        if (!hasPermissions) {
            return responseFactory.createForbiddenResponse();
        }

        try {
            return runPostConfig(restModel);
        } catch (AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(restModel.getJobId(), e.getMessage());
        }
    }

    private ResponseEntity<String> runPostConfig(JobFieldModel restModel) throws AlertException {
        String id = restModel.getJobId();
        if (StringUtils.isNotBlank(id) && jobConfigActions.doesJobExist(id)) {
            return responseFactory.createConflictResponse(id, "Provided id must not be in use. To update an existing configuration, use PUT.");
        }

        try {
            JobFieldModel updatedEntity = jobConfigActions.saveJob(restModel);
            return responseFactory.createCreatedResponse(updatedEntity.getJobId(), "Created");
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, "There were errors with the configuration.", e.getFieldErrors());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> putConfig(@PathVariable UUID id, @RequestBody(required = true) JobFieldModel restModel) {
        boolean hasPermissions = hasRequiredPermissions(restModel.getFieldModels(), authorizationManager::hasWritePermission);
        if (!hasPermissions) {
            return responseFactory.createForbiddenResponse();
        }

        try {
            return runPutConfig(id, restModel);
        } catch (AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(restModel.getJobId(), e.getMessage());
        }
    }

    private ResponseEntity<String> runPutConfig(UUID id, JobFieldModel restModel) throws AlertException {
        if (!jobConfigActions.doesJobExist(id)) {
            return responseFactory.createBadRequestResponse(id.toString(), "No configuration with the specified id.");
        }

        try {
            JobFieldModel updatedEntity = jobConfigActions.updateJob(id, restModel);
            return responseFactory.createAcceptedResponse(updatedEntity.getJobId(), "Updated");
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id.toString(), "There were errors with the configuration.", e.getFieldErrors());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConfig(@PathVariable UUID id) {
        if (null == id) {
            responseFactory.createBadRequestResponse("", "Proper ID is required for deleting.");
        }
        String stringId = contentConverter.getStringValue(id);
        try {
            if (jobConfigActions.doesJobExist(id)) {
                Optional<JobFieldModel> optionalModel = jobConfigActions.getJobById(id);

                if (optionalModel.isPresent()) {
                    JobFieldModel jobFieldModel = optionalModel.get();
                    boolean hasPermissions = hasRequiredPermissions(jobFieldModel.getFieldModels(), authorizationManager::hasDeletePermission);
                    if (!hasPermissions) {
                        return responseFactory.createForbiddenResponse();
                    }
                }

                jobConfigActions.deleteJobById(id);
                return responseFactory.createAcceptedResponse(stringId, "Deleted");
            } else {
                return responseFactory.createBadRequestResponse(stringId, "No configuration with the specified id.");
            }
        } catch (AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(stringId, e.getMessage());
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateConfig() {
        try {
            List<PermissionKey> keys = new LinkedList<>();
            for (Descriptor descriptor : descriptorMap.getDescriptorMap().values()) {
                DescriptorKey descriptorKey = descriptor.getDescriptorKey();
                for (ConfigContextEnum context : ConfigContextEnum.values()) {
                    if (descriptor.hasUIConfigForType(context)) {
                        keys.add(new PermissionKey(context.name(), descriptorKey.getUniversalKey()));
                    }
                }
            }
            if (!authorizationManager.anyReadPermission(keys)) {
                return responseFactory.createForbiddenResponse();
            }

            return responseFactory.createOkContentResponse(contentConverter.getJsonString(jobConfigActions.validateJobs()));
        } catch (AlertException ex) {
            logger.error(ex.getMessage(), ex);
            return responseFactory.createInternalServerErrorResponse(ResponseFactory.EMPTY_ID, ex.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateConfig(@RequestBody JobFieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }

        boolean hasPermissions = restModel.getFieldModels()
                                     .stream()
                                     .allMatch(model ->
                                                   authorizationManager.hasCreatePermission(model.getContext(), model.getDescriptorName())
                                                       || authorizationManager.hasWritePermission(model.getContext(), model.getDescriptorName())
                                                       || authorizationManager.hasExecutePermission(model.getContext(), model.getDescriptorName()));
        if (!hasPermissions) {
            return responseFactory.createForbiddenResponse();
        }

        String id = restModel.getJobId();
        try {
            String responseMessage = jobConfigActions.validateJob(restModel);
            return responseFactory.createOkResponse(id, responseMessage);
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, e.getMessage(), e.getFieldErrors());
        }
    }

    @PostMapping("/test")
    public ResponseEntity<String> testConfig(@RequestBody JobFieldModel restModel, @RequestParam(required = false) String destination) {
        return sendCustomMessage(restModel, (JobFieldModel jobModel) -> jobConfigActions.testJob(jobModel, destination));
    }

    private ResponseEntity<String> sendCustomMessage(JobFieldModel restModel, ThrowingFunction<JobFieldModel, String, IntegrationException> messageFunction) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }
        boolean hasPermissions = hasRequiredPermissions(restModel.getFieldModels(), authorizationManager::hasExecutePermission);
        if (!hasPermissions) {
            return responseFactory.createForbiddenResponse();
        }
        String id = restModel.getJobId();
        try {
            String responseMessage = messageFunction.apply(restModel);
            return responseFactory.createOkResponse(id, responseMessage);
        } catch (IntegrationRestException e) {
            String exceptionMessage = e.getMessage();
            logger.error(exceptionMessage, e);
            String message = exceptionMessage;
            if (StringUtils.isNotBlank(e.getHttpStatusMessage())) {
                message += " : " + e.getHttpStatusMessage();
            }
            return responseFactory.createMessageResponse(HttpStatus.valueOf(e.getHttpStatusCode()), id, message);
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, e.getMessage(), e.getFieldErrors());
        } catch (AlertMethodNotAllowedException e) {
            return responseFactory.createMethodNotAllowedResponse(e.getMessage());
        } catch (IntegrationException e) {
            return pkixErrorResponseFactory.createSSLExceptionResponse(id, e).orElse(responseFactory.createBadRequestResponse(id, e.getMessage()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return pkixErrorResponseFactory.createSSLExceptionResponse(id, e).orElse(responseFactory.createInternalServerErrorResponse(id, e.getMessage()));
        }
    }

    private boolean hasRequiredPermissions(Collection<FieldModel> fieldModels, BiFunction<String, String, Boolean> permissionChecker) {
        return fieldModels
                   .stream()
                   .allMatch(model -> permissionChecker.apply(model.getContext(), model.getDescriptorName()));
    }

}
