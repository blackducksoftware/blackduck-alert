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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JobConfigActions {
    private static final Logger logger = LoggerFactory.getLogger(JobConfigActions.class);
    private final ConfigurationAccessor configurationAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final DescriptorProcessor descriptorProcessor;
    private final ConfigurationFieldModelConverter modelConverter;

    @Autowired
    public JobConfigActions(final ConfigurationAccessor configurationAccessor, final FieldModelProcessor fieldModelProcessor, final DescriptorProcessor descriptorProcessor, final ConfigurationFieldModelConverter modelConverter) {
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.descriptorProcessor = descriptorProcessor;
        this.modelConverter = modelConverter;
    }

    public boolean doesJobExist(final UUID id) throws AlertDatabaseConstraintException {
        return null != id && configurationAccessor.getJobById(id).isPresent();
    }

    public boolean doesJobExist(final String id) throws AlertDatabaseConstraintException {
        return doesJobExist(UUID.fromString(id));
    }

    public Optional<JobFieldModel> getJobById(final UUID id) throws AlertException {
        final Optional<ConfigurationJobModel> jobConfiguration = configurationAccessor.getJobById(id);
        if (jobConfiguration.isPresent()) {
            final JobFieldModel jobFieldModel = readJobConfiguration(jobConfiguration.get());
            return Optional.of(jobFieldModel);
        }
        return Optional.empty();
    }

    public List<JobFieldModel> getAllJobs() throws AlertException {
        final List<ConfigurationJobModel> allJobs = configurationAccessor.getAllJobs();
        final List<JobFieldModel> jobFieldModels = new LinkedList<>();
        for (final ConfigurationJobModel configurationJobModel : allJobs) {
            final JobFieldModel jobFieldModel = readJobConfiguration(configurationJobModel);
            jobFieldModels.add(jobFieldModel);
        }
        return jobFieldModels;
    }

    public void deleteJobById(final UUID id) throws AlertException {
        final Optional<ConfigurationJobModel> jobs = configurationAccessor.getJobById(id);
        if (jobs.isPresent()) {
            final LinkedList<String> descriptorNames = new LinkedList<>();
            final ConfigurationJobModel configurationJobModel = jobs.get();
            for (final ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
                final FieldModel convertedFieldModel = modelConverter.convertToFieldModel(configurationModel);
                final FieldModel fieldModel = fieldModelProcessor.performBeforeDeleteAction(convertedFieldModel);
                descriptorNames.add(fieldModel.getDescriptorName());
            }
            configurationAccessor.deleteJob(configurationJobModel.getJobId());
            for (final String descriptorName : descriptorNames) {
                fieldModelProcessor.performAfterDeleteAction(descriptorName, ConfigContextEnum.DISTRIBUTION.name());
            }
        }
    }

    public JobFieldModel saveJob(final JobFieldModel jobFieldModel) throws AlertException {
        validateJob(jobFieldModel);
        validateJobNameUnique(jobFieldModel);
        final Set<String> descriptorNames = new HashSet<>();
        final Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
        for (final FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            final FieldModel beforeSaveEventFieldModel = fieldModelProcessor.performBeforeSaveAction(fieldModel);
            descriptorNames.add(beforeSaveEventFieldModel.getDescriptorName());
            final Collection<ConfigurationFieldModel> savedFieldsModels = modelConverter.convertToConfigurationFieldModelMap(beforeSaveEventFieldModel).values();
            configurationFieldModels.addAll(savedFieldsModels);
        }
        final ConfigurationJobModel savedJob = configurationAccessor.createJob(descriptorNames, configurationFieldModels);
        final JobFieldModel savedJobFieldModel = convertToJobFieldModel(savedJob);

        final Set<FieldModel> updatedFieldModels = new HashSet<>();
        for (final FieldModel fieldModel : savedJobFieldModel.getFieldModels()) {
            final FieldModel updatedModel = fieldModelProcessor.performAfterSaveAction(fieldModel);
            updatedFieldModels.add(updatedModel);
        }
        savedJobFieldModel.setFieldModels(updatedFieldModels);
        return savedJobFieldModel;
    }

    public JobFieldModel updateJob(final UUID id, final JobFieldModel jobFieldModel) throws AlertException {
        validateJob(jobFieldModel);
        final Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
        final Set<String> descriptorNames = new HashSet<>();
        for (final FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            final FieldModel beforeUpdateEventFieldModel = fieldModelProcessor.performBeforeUpdateAction(fieldModel);
            descriptorNames.add(beforeUpdateEventFieldModel.getDescriptorName());
            final String beforeFieldModelId = beforeUpdateEventFieldModel.getId();
            final Long fieldModelId = (StringUtils.isNotBlank(beforeFieldModelId)) ? Long.parseLong(beforeFieldModelId) : null;
            final Collection<ConfigurationFieldModel> updatedFieldModels = fieldModelProcessor.fillFieldModelWithExistingData(fieldModelId, beforeUpdateEventFieldModel);
            configurationFieldModels.addAll(updatedFieldModels);
        }

        final ConfigurationJobModel configurationJobModel = configurationAccessor.updateJob(id, descriptorNames, configurationFieldModels);
        final JobFieldModel savedJobFieldModel = convertToJobFieldModel(configurationJobModel);
        final Set<FieldModel> updatedFieldModels = new HashSet<>();
        for (final FieldModel fieldModel : savedJobFieldModel.getFieldModels()) {
            final FieldModel updatedModel = fieldModelProcessor.performAfterUpdateAction(fieldModel);
            updatedFieldModels.add(updatedModel);
        }
        savedJobFieldModel.setFieldModels(updatedFieldModels);
        return savedJobFieldModel;
    }

    private void validateJobNameUnique(final JobFieldModel jobFieldModel) throws AlertFieldException {
        for (final FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            validateJobNameUnique(fieldModel);
        }
    }

    private void validateJobNameUnique(final FieldModel fieldModel) throws AlertFieldException {
        final Optional<FieldValueModel> jobNameFieldOptional = fieldModel.getFieldValueModel(ChannelDistributionUIConfig.KEY_NAME);
        String error = "";
        if (jobNameFieldOptional.isPresent()) {
            final String jobName = jobNameFieldOptional.get().getValue().orElse(null);
            if (StringUtils.isNotBlank(jobName)) {
                final List<ConfigurationJobModel> jobs = configurationAccessor.getAllJobs();
                final Boolean foundDuplicateName = jobs.stream()
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
            final Map<String, String> fieldErrors = new HashMap<>();
            fieldErrors.put(ChannelDistributionUIConfig.KEY_NAME, error);
            throw new AlertFieldException(fieldErrors);
        }
    }

    public String validateJob(final JobFieldModel jobFieldModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        for (final FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            fieldErrors.putAll(fieldModelProcessor.validateFieldModel(fieldModel));
        }

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public String testJob(final JobFieldModel jobFieldModel, final String destination) throws IntegrationException {
        validateJob(jobFieldModel);
        FieldModel channelFieldModel = null;
        final Collection<FieldModel> otherJobModels = new LinkedList<>();
        for (final FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            final Optional<Descriptor> descriptor = descriptorProcessor.retrieveDescriptor(fieldModel.getDescriptorName());
            final FieldModel updatedFieldModel = fieldModelProcessor.createTestFieldModel(fieldModel);
            if (descriptor.filter(foundDescriptor -> DescriptorType.CHANNEL.equals(foundDescriptor.getType())).isPresent()) {
                channelFieldModel = updatedFieldModel;
            } else {
                otherJobModels.add(updatedFieldModel);
            }
        }

        if (null != channelFieldModel) {
            final Optional<TestAction> testActionOptional = descriptorProcessor.retrieveTestAction(channelFieldModel);
            if (testActionOptional.isPresent()) {
                final Map<String, ConfigurationFieldModel> fields = new HashMap<>();

                fields.putAll(modelConverter.convertToConfigurationFieldModelMap(channelFieldModel));
                final Optional<ConfigurationModel> configurationFieldModel = configurationAccessor.getConfigurationByDescriptorNameAndContext(channelFieldModel.getDescriptorName(), ConfigContextEnum.GLOBAL).stream().findFirst();

                configurationFieldModel.ifPresent(model -> fields.putAll(model.getCopyOfKeyToFieldMap()));

                for (final FieldModel fieldModel : otherJobModels) {
                    fields.putAll(modelConverter.convertToConfigurationFieldModelMap(fieldModel));
                }

                final TestAction testAction = testActionOptional.get();
                final FieldAccessor fieldAccessor = new FieldAccessor(fields);
                final TestConfigModel testConfig = testAction.createTestConfigModel(channelFieldModel.getId(), fieldAccessor, destination);
                final Optional<TestAction> providerTestAction = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                                                    .flatMap(providerName -> descriptorProcessor.retrieveTestAction(providerName, ConfigContextEnum.DISTRIBUTION));
                if (providerTestAction.isPresent()) {
                    providerTestAction.get().testConfig(testConfig);
                }
                return testAction.testConfig(testConfig);
            } else {
                final String descriptorName = channelFieldModel.getDescriptorName();
                logger.error("Test action did not exist: {}", descriptorName);
                throw new AlertMethodNotAllowedException("Test functionality not implemented for " + descriptorName);
            }
        }
        return "No field model of type channel was was sent to test.";
    }

    private JobFieldModel readJobConfiguration(final ConfigurationJobModel groupedConfiguration) throws AlertException {
        final Set<ConfigurationModel> configurations = groupedConfiguration.getCopyOfConfigurations();
        final Set<FieldModel> constructedFieldModels = new HashSet<>();
        for (final ConfigurationModel configurationModel : configurations) {
            final FieldModel fieldModel = modelConverter.convertToFieldModel(configurationModel);
            constructedFieldModels.add(fieldModelProcessor.performAfterReadAction(fieldModel));
        }
        return new JobFieldModel(groupedConfiguration.getJobId().toString(), constructedFieldModels);
    }

    private JobFieldModel convertToJobFieldModel(final ConfigurationJobModel configurationJobModel) throws AlertDatabaseConstraintException {
        final Set<FieldModel> constructedFieldModels = new HashSet<>();
        for (final ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
            constructedFieldModels.add(modelConverter.convertToFieldModel(configurationModel));
        }
        return new JobFieldModel(configurationJobModel.getJobId().toString(), constructedFieldModels);
    }

}
