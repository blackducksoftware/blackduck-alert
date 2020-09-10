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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.action.api.AbstractJobResourceActions;
import com.synopsys.integration.alert.common.action.api.AbstractResourceActions;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.GlobalConfigExistsValidator;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldStatuses;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.web.common.PKIXErrorResponseFactory;
import com.synopsys.integration.alert.web.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.web.common.field.FieldModelProcessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class JobConfigActions extends AbstractJobResourceActions {
    private final Logger logger = LoggerFactory.getLogger(JobConfigActions.class);
    private final ConfigurationAccessor configurationAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final DescriptorProcessor descriptorProcessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final GlobalConfigExistsValidator globalConfigExistsValidator;
    private final PKIXErrorResponseFactory pkixErrorResponseFactory;
    private final DescriptorMap descriptorMap;

    @Autowired
    public JobConfigActions(AuthorizationManager authorizationManager, DescriptorAccessor descriptorAccessor, ConfigurationAccessor configurationAccessor, FieldModelProcessor fieldModelProcessor, DescriptorProcessor descriptorProcessor,
        ConfigurationFieldModelConverter modelConverter, GlobalConfigExistsValidator globalConfigExistsValidator, PKIXErrorResponseFactory pkixErrorResponseFactory, DescriptorMap descriptorMap) {
        super(authorizationManager, descriptorAccessor);
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.descriptorProcessor = descriptorProcessor;
        this.modelConverter = modelConverter;
        this.globalConfigExistsValidator = globalConfigExistsValidator;
        this.pkixErrorResponseFactory = pkixErrorResponseFactory;
        this.descriptorMap = descriptorMap;
    }

    @Override
    protected Optional<JobFieldModel> findJobFieldModel(UUID id) {
        try {
            Optional<ConfigurationJobModel> jobConfiguration = configurationAccessor.getJobById(id);
            if (jobConfiguration.isPresent()) {
                JobFieldModel jobFieldModel = readJobConfiguration(jobConfiguration.get());
                return Optional.of(jobFieldModel);
            }
        } catch (AlertException ex) {
            logger.error(String.format("Error finding job configuration id: %s", id), ex);
        }
        return Optional.empty();
    }

    @Override
    protected ActionResponse<List<JobFieldModel>> readAllResources() {
        try {
            List<ConfigurationJobModel> allJobs = configurationAccessor.getAllJobs();
            List<JobFieldModel> jobFieldModels = new LinkedList<>();
            for (ConfigurationJobModel configurationJobModel : allJobs) {
                JobFieldModel jobFieldModel = readJobConfiguration(configurationJobModel);
                jobFieldModels.add(jobFieldModel);
            }
            return new ActionResponse<>(HttpStatus.OK, jobFieldModels);
        } catch (AlertException ex) {
            logger.error("Error reading all jobs", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected ActionResponse<JobFieldModel> deleteResource(UUID id) {
        try {
            Optional<ConfigurationJobModel> jobs = configurationAccessor.getJobById(id);
            if (jobs.isPresent()) {
                LinkedList<FieldModel> processedFieldModels = new LinkedList<>();
                ConfigurationJobModel configurationJobModel = jobs.get();
                for (ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
                    FieldModel convertedFieldModel = modelConverter.convertToFieldModel(configurationModel);
                    FieldModel fieldModel = fieldModelProcessor.performBeforeDeleteAction(convertedFieldModel);
                    processedFieldModels.add(fieldModel);
                }
                configurationAccessor.deleteJob(configurationJobModel.getJobId());
                for (FieldModel fieldModel : processedFieldModels) {
                    fieldModelProcessor.performAfterDeleteAction(fieldModel);
                }

            }
        } catch (AlertException ex) {
            logger.error("Error reading all jobs", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

        return new ActionResponse<>(HttpStatus.NO_CONTENT, null);
    }

    @Override
    protected ActionResponse<JobFieldModel> createResource(JobFieldModel resource) {
        try {
            Set<String> descriptorNames = new HashSet<>();
            Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
            for (FieldModel fieldModel : resource.getFieldModels()) {
                FieldModel beforeSaveEventFieldModel = fieldModelProcessor.performBeforeSaveAction(fieldModel);
                descriptorNames.add(beforeSaveEventFieldModel.getDescriptorName());
                Collection<ConfigurationFieldModel> savedFieldsModels = modelConverter.convertToConfigurationFieldModelMap(beforeSaveEventFieldModel).values();
                configurationFieldModels.addAll(savedFieldsModels);
            }
            ConfigurationJobModel savedJob = configurationAccessor.createJob(descriptorNames, configurationFieldModels);
            JobFieldModel savedJobFieldModel = convertToJobFieldModel(savedJob);

            Set<FieldModel> updatedFieldModels = new HashSet<>();
            for (FieldModel fieldModel : savedJobFieldModel.getFieldModels()) {
                FieldModel updatedModel = fieldModelProcessor.performAfterSaveAction(fieldModel);
                updatedFieldModels.add(updatedModel);
            }
            savedJobFieldModel.setFieldModels(updatedFieldModels);
            return new ActionResponse<>(HttpStatus.OK, savedJobFieldModel);
        } catch (AlertException ex) {
            logger.error("Error creating job", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected ActionResponse<JobFieldModel> updateResource(UUID id, JobFieldModel resource) {
        try {
            ConfigurationJobModel previousJob = configurationAccessor.getJobById(id)
                                                    .orElseThrow(() -> new IllegalStateException("No previous job present when the only possible valid state for this stage of the method would require it"));
            Map<String, FieldModel> descriptorAndContextToPreviousFieldModel = new HashMap<>();
            for (ConfigurationModel previousJobConfiguration : previousJob.getCopyOfConfigurations()) {
                FieldModel previousJobFieldModel = modelConverter.convertToFieldModel(previousJobConfiguration);
                descriptorAndContextToPreviousFieldModel.put(previousJobFieldModel.getDescriptorName() + previousJobFieldModel.getContext(), previousJobFieldModel);
            }

            Set<String> descriptorNames = new HashSet<>();
            Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
            for (FieldModel fieldModel : resource.getFieldModels()) {
                FieldModel beforeUpdateEventFieldModel = fieldModelProcessor.performBeforeUpdateAction(fieldModel);
                descriptorNames.add(beforeUpdateEventFieldModel.getDescriptorName());
                String beforeFieldModelId = beforeUpdateEventFieldModel.getId();
                Long fieldModelId = (StringUtils.isNotBlank(beforeFieldModelId)) ? Long.parseLong(beforeFieldModelId) : null;
                Collection<ConfigurationFieldModel> updatedFieldModels = fieldModelProcessor.fillFieldModelWithExistingData(fieldModelId, beforeUpdateEventFieldModel);
                configurationFieldModels.addAll(updatedFieldModels);
            }

            ConfigurationJobModel configurationJobModel = configurationAccessor.updateJob(id, descriptorNames, configurationFieldModels);
            JobFieldModel savedJobFieldModel = convertToJobFieldModel(configurationJobModel);
            Set<FieldModel> updatedFieldModels = new HashSet<>();
            for (FieldModel fieldModel : savedJobFieldModel.getFieldModels()) {
                FieldModel previousFieldModel = descriptorAndContextToPreviousFieldModel.get(fieldModel.getDescriptorName() + fieldModel.getContext());
                FieldModel updatedModel = fieldModelProcessor.performAfterUpdateAction(previousFieldModel, fieldModel);
                updatedFieldModels.add(updatedModel);
            }
            savedJobFieldModel.setFieldModels(updatedFieldModels);
            return new ActionResponse<>(HttpStatus.OK, savedJobFieldModel);
        } catch (AlertException ex) {
            logger.error("Error creating job", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private ValidationResponseModel validateJobNameUnique(@Nullable UUID currentJobId, JobFieldModel jobFieldModel) {
        ValidationResponseModel responseModel = ValidationResponseModel.withoutFieldStatuses("Valid name");
        for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            responseModel = validateJobNameUnique(currentJobId, fieldModel);
            if (responseModel.hasErrors()) {
                return responseModel;
            }
        }

        return responseModel;
    }

    private ValidationResponseModel validateJobNameUnique(@Nullable UUID currentJobId, FieldModel fieldModel) {
        Optional<FieldValueModel> jobNameFieldOptional = fieldModel.getFieldValueModel(ChannelDistributionUIConfig.KEY_NAME);
        String error = "";
        if (jobNameFieldOptional.isPresent()) {
            String jobName = jobNameFieldOptional.get().getValue().orElse(null);
            if (StringUtils.isNotBlank(jobName)) {
                List<ConfigurationJobModel> jobs = configurationAccessor.getAllJobs();

                boolean foundDuplicateName = jobs.stream()
                                                 .filter(job -> filterOutMatchingJobs(currentJobId, job))
                                                 .flatMap(job -> job.getCopyOfConfigurations().stream())
                                                 .map(configurationModel -> configurationModel.getField(ChannelDistributionUIConfig.KEY_NAME).orElse(null))
                                                 .filter(configurationFieldModel -> (null != configurationFieldModel) && configurationFieldModel.getFieldValue().isPresent())
                                                 .anyMatch(configurationFieldModel -> jobName.equals(configurationFieldModel.getFieldValue().get()));
                if (foundDuplicateName) {
                    error = "A distribution configuration with this name already exists.";
                }
            } else {
                error = "Name cannot be blank.";
            }
        }
        if (StringUtils.isNotBlank(error)) {
            AlertFieldStatus fieldStatus = AlertFieldStatus.error(ChannelDistributionUIConfig.KEY_NAME, error);
            return ValidationResponseModel.fromStatusCollection("Job name not unique", List.of(fieldStatus));
        }
        return ValidationResponseModel.withoutFieldStatuses("Job Name Vaild");
    }

    private boolean filterOutMatchingJobs(@Nullable UUID currentJobId, ConfigurationJobModel configurationJobModel) {
        if (null != currentJobId && null != configurationJobModel.getJobId()) {
            return !configurationJobModel.getJobId().equals(currentJobId);
        } else {
            return true;
        }
    }

    @Override
    protected ValidationActionResponse validateResource(JobFieldModel resource) {
        List<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        UUID jobId = null;
        if (StringUtils.isNotBlank(resource.getJobId())) {
            jobId = UUID.fromString(resource.getJobId());
        }
        ValidationResponseModel responseModel = validateJobNameUnique(jobId, resource);
        if (responseModel.hasErrors()) {
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        }
        for (FieldModel fieldModel : resource.getFieldModels()) {
            fieldStatuses.addAll(fieldModelProcessor.validateFieldModel(fieldModel));
        }

        if (fieldStatuses.isEmpty()) {
            responseModel = ValidationResponseModel.withoutFieldStatuses("Valid");
        } else {
            responseModel = ValidationResponseModel.fromStatusCollection("Invalid", fieldStatuses);
        }
        return new ValidationActionResponse(HttpStatus.OK, responseModel);
    }

    public ActionResponse<List<JobFieldStatuses>> validateAllJobs() {
        List<PermissionKey> keys = new LinkedList<>();
        for (Descriptor descriptor : descriptorMap.getDescriptorMap().values()) {
            DescriptorKey descriptorKey = descriptor.getDescriptorKey();
            for (ConfigContextEnum context : ConfigContextEnum.values()) {
                if (descriptor.hasUIConfigForType(context)) {
                    keys.add(new PermissionKey(context.name(), descriptorKey.getUniversalKey()));
                }
            }
        }
        if (!getAuthorizationManager().anyReadPermission(keys)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }

        List<JobFieldStatuses> errorsList = new LinkedList<>();
        List<JobFieldModel> jobFieldModels = readAllResources().getContent().orElse(List.of());
        for (JobFieldModel jobFieldModel : jobFieldModels) {
            List<AlertFieldStatus> fieldErrors = new ArrayList<>();
            for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
                fieldErrors.addAll(fieldModelProcessor.validateFieldModel(fieldModel));
            }
            if (!fieldErrors.isEmpty()) {
                errorsList.add(new JobFieldStatuses(jobFieldModel.getJobId(), fieldErrors));
            }
        }

        return new ActionResponse<>(HttpStatus.OK, errorsList);
    }

    @Override
    protected ValidationActionResponse testResource(JobFieldModel resource) {
        ValidationResponseModel responseModel;
        String id = resource.getJobId();
        try {

            Collection<FieldModel> otherJobModels = new LinkedList<>();
            FieldModel channelFieldModel = getChannelFieldModelAndPopulateOtherJobModels(resource, otherJobModels);

            if (null != channelFieldModel) {
                Optional<TestAction> testActionOptional = descriptorProcessor.retrieveTestAction(channelFieldModel);
                if (testActionOptional.isPresent()) {
                    Map<String, ConfigurationFieldModel> fields = createFieldsMap(channelFieldModel, otherJobModels);
                    // The custom message fields are not written to the database or defined fields in the database.  Need to manually add them.
                    // TODO Create a mechanism to create the field accessor with a combination of fields in the database and fields that are not.
                    Optional<ConfigurationFieldModel> topicField = convertFieldToConfigurationField(channelFieldModel, TestAction.KEY_CUSTOM_TOPIC);
                    Optional<ConfigurationFieldModel> messageField = convertFieldToConfigurationField(channelFieldModel, TestAction.KEY_CUSTOM_MESSAGE);
                    topicField.ifPresent(model -> fields.put(TestAction.KEY_CUSTOM_TOPIC, model));
                    messageField.ifPresent(model -> fields.put(TestAction.KEY_CUSTOM_MESSAGE, model));
                    TestAction testAction = testActionOptional.get();
                    FieldAccessor fieldAccessor = new FieldAccessor(fields);
                    String jobId = channelFieldModel.getId();

                    MessageResult providerTestResult = testProviderConfig(fieldAccessor, jobId, channelFieldModel);
                    if (providerTestResult.hasErrors()) {
                        responseModel = ValidationResponseModel.fromStatusCollection(providerTestResult.getStatusMessage(), providerTestResult.getFieldStatuses());
                        return new ValidationActionResponse(HttpStatus.OK, responseModel);
                    }

                    MessageResult testActionResult = testAction.testConfig(jobId, channelFieldModel, fieldAccessor);
                    List<AlertFieldStatus> resultFieldStatuses = testActionResult.getFieldStatuses();
                    responseModel = ValidationResponseModel.fromStatusCollection(testActionResult.getStatusMessage(), resultFieldStatuses);
                    return new ValidationActionResponse(HttpStatus.OK, responseModel);
                } else {
                    String descriptorName = channelFieldModel.getDescriptorName();
                    logger.error("Test action did not exist: {}", descriptorName);
                    responseModel = ValidationResponseModel.withoutFieldStatuses("Test functionality not implemented for " + descriptorName);
                    return new ValidationActionResponse(HttpStatus.METHOD_NOT_ALLOWED, responseModel);
                }
            }
            responseModel = ValidationResponseModel.withoutFieldStatuses("No field model of type channel was was sent to test.");
            return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
        } catch (IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return ValidationActionResponse.createResponseFromIntegrationRestException(e);
        } catch (AlertFieldException e) {
            logger.error("Test Error with field Errors", e);
            responseModel = ValidationResponseModel.fromStatusCollection(e.getMessage(), e.getFieldErrors());
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        } catch (AlertMethodNotAllowedException e) {
            logger.error(e.getMessage(), e);
            return new ValidationActionResponse(HttpStatus.METHOD_NOT_ALLOWED, ValidationResponseModel.withoutFieldStatuses(e.getMessage()));
        } catch (IntegrationException e) {
            responseModel = pkixErrorResponseFactory.createSSLExceptionResponse(id, e)
                                .orElse(ValidationResponseModel.withoutFieldStatuses(e.getMessage()));
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            responseModel = pkixErrorResponseFactory.createSSLExceptionResponse(id, e)
                                .orElse(ValidationResponseModel.withoutFieldStatuses(e.getMessage()));
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        }
    }

    public ActionResponse<String> checkGlobalConfigExists(String descriptorName) {
        Optional<String> configMissingMessage = globalConfigExistsValidator.validate(descriptorName);
        if (configMissingMessage.isPresent()) {
            String message = configMissingMessage.get();
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, message);
        }
        return new ActionResponse<>(HttpStatus.NO_CONTENT, null);
    }

    private FieldModel getChannelFieldModelAndPopulateOtherJobModels(JobFieldModel jobFieldModel, Collection<FieldModel> otherJobModels) throws AlertException {
        FieldModel channelFieldModel = null;
        for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            Optional<Descriptor> descriptor = descriptorProcessor.retrieveDescriptor(fieldModel.getDescriptorName());
            FieldModel updatedFieldModel = fieldModelProcessor.createCustomMessageFieldModel(fieldModel);
            if (descriptor.filter(foundDescriptor -> DescriptorType.CHANNEL.equals(foundDescriptor.getType())).isPresent()) {
                channelFieldModel = updatedFieldModel;
            } else {
                otherJobModels.add(updatedFieldModel);
            }
        }
        return channelFieldModel;
    }

    private Map<String, ConfigurationFieldModel> createFieldsMap(FieldModel channelFieldModel, Collection<FieldModel> otherJobModels) throws AlertDatabaseConstraintException {
        Map<String, ConfigurationFieldModel> fields = new HashMap<>();

        fields.putAll(modelConverter.convertToConfigurationFieldModelMap(channelFieldModel));
        Optional<ConfigurationModel> configurationFieldModel = configurationAccessor.getConfigurationsByDescriptorNameAndContext(channelFieldModel.getDescriptorName(), ConfigContextEnum.GLOBAL).stream().findFirst();

        configurationFieldModel.ifPresent(model -> fields.putAll(model.getCopyOfKeyToFieldMap()));

        for (FieldModel fieldModel : otherJobModels) {
            fields.putAll(modelConverter.convertToConfigurationFieldModelMap(fieldModel));
        }
        return fields;
    }

    private MessageResult testProviderConfig(FieldAccessor fieldAccessor, String jobId, FieldModel fieldModel) throws IntegrationException {
        Optional<TestAction> providerTestAction = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                                      .flatMap(providerName -> descriptorProcessor.retrieveTestAction(providerName, ConfigContextEnum.DISTRIBUTION));
        if (providerTestAction.isPresent()) {
            return providerTestAction.get().testConfig(jobId, fieldModel, fieldAccessor);
        }
        return new MessageResult("Provider Config Valid");
    }

    private JobFieldModel readJobConfiguration(ConfigurationJobModel groupedConfiguration) throws AlertException {
        Set<ConfigurationModel> configurations = groupedConfiguration.getCopyOfConfigurations();
        Set<FieldModel> constructedFieldModels = new HashSet<>();
        for (ConfigurationModel configurationModel : configurations) {
            FieldModel fieldModel = modelConverter.convertToFieldModel(configurationModel);
            constructedFieldModels.add(fieldModelProcessor.performAfterReadAction(fieldModel));
        }
        return new JobFieldModel(groupedConfiguration.getJobId().toString(), constructedFieldModels);
    }

    private JobFieldModel convertToJobFieldModel(ConfigurationJobModel configurationJobModel) throws AlertDatabaseConstraintException {
        Set<FieldModel> constructedFieldModels = new HashSet<>();
        for (ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
            constructedFieldModels.add(modelConverter.convertToFieldModel(configurationModel));
        }
        return new JobFieldModel(configurationJobModel.getJobId().toString(), constructedFieldModels);
    }

    private Optional<ConfigurationFieldModel> convertFieldToConfigurationField(FieldModel fieldModel, String fieldKey) {
        Optional<FieldValueModel> fieldValueModel = fieldModel.getFieldValueModel(fieldKey);
        if (fieldValueModel.isPresent()) {
            ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(fieldKey);
            configurationFieldModel.setFieldValues(fieldValueModel.get().getValues());
            return Optional.of(configurationFieldModel);
        }
        return Optional.empty();
    }

    private List<AlertFieldStatus> concatStatuses(List<AlertFieldStatus> firstStatuses, List<AlertFieldStatus> secondStatuses) {
        return Stream.concat(
            firstStatuses.stream(),
            secondStatuses.stream()
        ).collect(Collectors.toList());
    }

}
