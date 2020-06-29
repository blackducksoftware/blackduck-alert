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
package com.synopsys.integration.alert.web.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.GlobalConfigExistsValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldErrors;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JobConfigActions {
    private final Logger logger = LoggerFactory.getLogger(JobConfigActions.class);
    private final ConfigurationAccessor configurationAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final DescriptorProcessor descriptorProcessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final GlobalConfigExistsValidator globalConfigExistsValidator;

    @Autowired
    public JobConfigActions(ConfigurationAccessor configurationAccessor, FieldModelProcessor fieldModelProcessor, DescriptorProcessor descriptorProcessor, ConfigurationFieldModelConverter modelConverter,
        GlobalConfigExistsValidator globalConfigExistsValidator) {
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.descriptorProcessor = descriptorProcessor;
        this.modelConverter = modelConverter;
        this.globalConfigExistsValidator = globalConfigExistsValidator;
    }

    public boolean doesJobExist(UUID id) throws AlertDatabaseConstraintException {
        return null != id && configurationAccessor.getJobById(id).isPresent();
    }

    public boolean doesJobExist(String id) throws AlertDatabaseConstraintException {
        return doesJobExist(UUID.fromString(id));
    }

    public Optional<JobFieldModel> getJobById(UUID id) throws AlertException {
        Optional<ConfigurationJobModel> jobConfiguration = configurationAccessor.getJobById(id);
        if (jobConfiguration.isPresent()) {
            JobFieldModel jobFieldModel = readJobConfiguration(jobConfiguration.get());
            return Optional.of(jobFieldModel);
        }
        return Optional.empty();
    }

    public List<JobFieldModel> getAllJobs() throws AlertException {
        List<ConfigurationJobModel> allJobs = configurationAccessor.getAllJobs();
        List<JobFieldModel> jobFieldModels = new LinkedList<>();
        for (ConfigurationJobModel configurationJobModel : allJobs) {
            JobFieldModel jobFieldModel = readJobConfiguration(configurationJobModel);
            jobFieldModels.add(jobFieldModel);
        }
        return jobFieldModels;
    }

    public void deleteJobById(UUID id) throws AlertException {
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
    }

    public JobFieldModel saveJob(JobFieldModel jobFieldModel) throws AlertException {
        validateJob(jobFieldModel);
        validateJobNameUnique(null, jobFieldModel);
        Set<String> descriptorNames = new HashSet<>();
        Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
        for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
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
        return savedJobFieldModel;
    }

    public JobFieldModel updateJob(UUID id, JobFieldModel jobFieldModel) throws AlertException {
        validateJob(jobFieldModel);
        validateJobNameUnique(id, jobFieldModel);
        
        ConfigurationJobModel previousJob = configurationAccessor.getJobById(id)
                                                .orElseThrow(() -> new IllegalStateException("No previous job present when the only possible valid state for this stage of the method would require it"));
        Map<String, FieldModel> descriptorAndContextToPreviousFieldModel = new HashMap<>();
        for (ConfigurationModel previousJobConfiguration : previousJob.getCopyOfConfigurations()) {
            FieldModel previousJobFieldModel = modelConverter.convertToFieldModel(previousJobConfiguration);
            descriptorAndContextToPreviousFieldModel.put(previousJobFieldModel.getDescriptorName() + previousJobFieldModel.getContext(), previousJobFieldModel);
        }

        Set<String> descriptorNames = new HashSet<>();
        Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
        for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
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
        return savedJobFieldModel;
    }

    private void validateJobNameUnique(@Nullable UUID currentJobId, JobFieldModel jobFieldModel) throws AlertFieldException {
        for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            validateJobNameUnique(currentJobId, fieldModel);
        }
    }

    private void validateJobNameUnique(@Nullable UUID currentJobId, FieldModel fieldModel) throws AlertFieldException {
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
            throw AlertFieldException.singleFieldError(ChannelDistributionUIConfig.KEY_NAME, error);
        }
    }

    private boolean filterOutMatchingJobs(@Nullable UUID currentJobId, ConfigurationJobModel configurationJobModel) {
        if (null != currentJobId && null != configurationJobModel.getJobId()) {
            return !configurationJobModel.getJobId().equals(currentJobId);
        } else {
            return true;
        }
    }

    public String validateJob(JobFieldModel jobFieldModel) throws AlertFieldException {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            fieldErrors.putAll(fieldModelProcessor.validateFieldModel(fieldModel));
        }

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public List<JobFieldErrors> validateJobs() throws AlertException {
        List<JobFieldErrors> errorsList = new LinkedList<>();
        List<JobFieldModel> jobFieldModels = getAllJobs();
        for (JobFieldModel jobFieldModel : jobFieldModels) {
            Map<String, String> fieldErrors = new HashMap<>();
            for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
                fieldErrors.putAll(fieldModelProcessor.validateFieldModel(fieldModel));
            }
            if (!fieldErrors.isEmpty()) {
                errorsList.add(new JobFieldErrors(jobFieldModel.getJobId(), fieldErrors));
            }
        }

        return errorsList;
    }

    // TODO abstract duplicate functionality
    public String testJob(JobFieldModel jobFieldModel) throws IntegrationException {
        validateJob(jobFieldModel);
        Collection<FieldModel> otherJobModels = new LinkedList<>();
        FieldModel channelFieldModel = getChannelFieldModelAndPopulateOtherJobModels(jobFieldModel, otherJobModels);

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

                testProviderConfig(fieldAccessor, jobId, channelFieldModel);
                MessageResult testResult = testAction.testConfig(jobId, channelFieldModel, fieldAccessor);
                return testResult.getStatusMessage();
            } else {
                String descriptorName = channelFieldModel.getDescriptorName();
                logger.error("Test action did not exist: {}", descriptorName);
                throw new AlertMethodNotAllowedException("Test functionality not implemented for " + descriptorName);
            }
        }
        return "No field model of type channel was was sent to test.";
    }

    public Optional<String> checkGlobalConfigExists(String descriptorName) {
        return globalConfigExistsValidator.validate(descriptorName);
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

    private void testProviderConfig(FieldAccessor fieldAccessor, String jobId, FieldModel fieldModel) throws IntegrationException {
        Optional<TestAction> providerTestAction = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                                      .flatMap(providerName -> descriptorProcessor.retrieveTestAction(providerName, ConfigContextEnum.DISTRIBUTION));
        if (providerTestAction.isPresent()) {
            providerTestAction.get().testConfig(jobId, fieldModel, fieldAccessor);
        }
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

}
