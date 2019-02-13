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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationJobModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.alert.web.model.configuration.JobFieldModel;
import com.synopsys.integration.alert.web.model.configuration.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JobConfigActions {
    private static final Logger logger = LoggerFactory.getLogger(JobConfigActions.class);
    private final BaseConfigurationAccessor configurationAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final ContentConverter contentConverter;
    private final ConfigurationFieldModelConverter modelConverter;

    @Autowired
    public JobConfigActions(final BaseConfigurationAccessor configurationAccessor, final FieldModelProcessor fieldModelProcessor, final ContentConverter contentConverter,
        final ConfigurationFieldModelConverter modelConverter) {
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.contentConverter = contentConverter;
        this.modelConverter = modelConverter;
    }

    public boolean doesJobExist(final UUID id) throws AlertDatabaseConstraintException {
        return null != id && configurationAccessor.getJobById(id).isPresent();
    }

    public boolean doesJobExist(final String id) throws AlertDatabaseConstraintException {
        return doesJobExist(UUID.fromString(id));
    }

    public Optional<JobFieldModel> getJobById(final UUID id) throws AlertDatabaseConstraintException {
        final Optional<ConfigurationJobModel> jobConfiguration = configurationAccessor.getJobById(id);
        if (jobConfiguration.isPresent()) {
            final JobFieldModel jobFieldModel = readJobConfiguration(jobConfiguration.get());
            return Optional.of(jobFieldModel);
        }
        return Optional.empty();
    }

    public List<JobFieldModel> getAllJobs() throws AlertDatabaseConstraintException {
        final List<ConfigurationJobModel> allJobs = configurationAccessor.getAllJobs();
        final List<JobFieldModel> jobFieldModels = new LinkedList<>();
        for (final ConfigurationJobModel configurationJobModel : allJobs) {
            final JobFieldModel jobFieldModel = readJobConfiguration(configurationJobModel);
            jobFieldModels.add(jobFieldModel);
        }
        return jobFieldModels;
    }

    public void deleteJobById(final UUID id) throws AlertDatabaseConstraintException {
        final Optional<ConfigurationJobModel> jobs = configurationAccessor.getJobById(id);
        if (jobs.isPresent()) {
            final ConfigurationJobModel configurationJobModel = jobs.get();
            for (final ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
                fieldModelProcessor.performDeleteAction(configurationModel);
            }
            configurationAccessor.deleteJob(configurationJobModel.getJobId());
        }
    }

    public JobFieldModel saveJob(final JobFieldModel jobFieldModel) throws AlertFieldException, AlertDatabaseConstraintException {
        validateJob(jobFieldModel);
        validateJobNameUnique(jobFieldModel);
        final Set<String> descriptorNames = new HashSet<>();
        final Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
        for (final FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            descriptorNames.add(fieldModel.getDescriptorName());
            final Collection<ConfigurationFieldModel> savedFieldsModels = modelConverter.convertFromFieldModel(fieldModel).values();
            configurationFieldModels.addAll(savedFieldsModels);
        }
        final ConfigurationJobModel savedJob = configurationAccessor.createJob(descriptorNames, configurationFieldModels);
        final JobFieldModel savedJobFieldModel = convertToJobFieldModel(savedJob);

        final Set<FieldModel> updatedFieldModels = savedJobFieldModel.getFieldModels()
                                                       .stream()
                                                       .map(fieldModel -> fieldModelProcessor.performSaveAction(fieldModel))
                                                       .collect(Collectors.toSet());
        savedJobFieldModel.setFieldModels(updatedFieldModels);
        return savedJobFieldModel;
    }

    public JobFieldModel updateJob(final UUID id, final JobFieldModel jobFieldModel) throws AlertFieldException, AlertException {
        validateJob(jobFieldModel);
        final Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
        for (final FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            final Long fieldModelId = contentConverter.getLongValue(fieldModel.getId());
            final Collection<ConfigurationFieldModel> updatedFieldModels = fieldModelProcessor.fillFieldModelWithExistingData(fieldModelId, fieldModel);
            configurationFieldModels.addAll(updatedFieldModels);
        }
        final ConfigurationJobModel configurationJobModel = configurationAccessor.updateJob(id, configurationFieldModels);
        final JobFieldModel savedJobFieldModel = convertToJobFieldModel(configurationJobModel);
        final Set<FieldModel> updatedFieldModels = savedJobFieldModel.getFieldModels()
                                                       .stream()
                                                       .map(fieldModel -> fieldModelProcessor.performUpdateAction(fieldModel))
                                                       .collect(Collectors.toSet());
        savedJobFieldModel.setFieldModels(updatedFieldModels);
        return savedJobFieldModel;
    }

    private void validateJobNameUnique(final JobFieldModel jobFieldModel) throws AlertFieldException {
        for (final FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            validateJobNameUnique(fieldModel);
        }
    }

    private void validateJobNameUnique(final FieldModel fieldModel) throws AlertFieldException {
        final String descriptorName = fieldModel.getDescriptorName();
        final Optional<FieldValueModel> jobNameFieldOptional = fieldModel.getField(ChannelDistributionUIConfig.KEY_NAME);
        String error = "";
        if (jobNameFieldOptional.isPresent()) {
            final String jobName = jobNameFieldOptional.get().getValue().orElse(null);
            if (StringUtils.isNotBlank(jobName)) {
                try {
                    final List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorName(descriptorName);
                    final Boolean foundDuplicateName = configurations.stream()
                                                           .map(configurationModel -> configurationModel.getField(ChannelDistributionUIConfig.KEY_NAME).orElse(null))
                                                           .filter(configurationFieldModel -> (null != configurationFieldModel) && configurationFieldModel.getFieldValue().isPresent())
                                                           .anyMatch(configurationFieldModel -> jobName.equals(configurationFieldModel.getFieldValue().get()));
                    if (foundDuplicateName) {
                        error = "A distribution configuration with this name already exists.";
                    }
                } catch (final AlertDatabaseConstraintException e) {
                    logger.error("Could not retrieve distributions of {}", jobName);
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
            final Optional<Descriptor> descriptor = fieldModelProcessor.retrieveDescriptor(fieldModel.getDescriptorName());
            final FieldModel updatedFieldModel = fieldModelProcessor.createTestFieldModel(fieldModel);
            if (descriptor.filter(foundDescriptor -> DescriptorType.CHANNEL.equals(foundDescriptor.getType())).isPresent()) {
                channelFieldModel = updatedFieldModel;
            } else {
                otherJobModels.add(updatedFieldModel);
            }
        }

        if (null != channelFieldModel) {
            final Optional<DescriptorActionApi> descriptorActionApi = fieldModelProcessor.retrieveDescriptorActionApi(channelFieldModel);
            if (descriptorActionApi.isPresent()) {
                final Map<String, ConfigurationFieldModel> fields = new HashMap<>();

                fields.putAll(modelConverter.convertFromFieldModel(channelFieldModel));
                final Optional<ConfigurationModel> configurationFieldModel = configurationAccessor.getConfigurationByDescriptorNameAndContext(channelFieldModel.getDescriptorName(), ConfigContextEnum.GLOBAL).stream().findFirst();

                configurationFieldModel.ifPresent(model -> fields.putAll(model.getCopyOfKeyToFieldMap()));

                for (final FieldModel fieldModel : otherJobModels) {
                    fields.putAll(modelConverter.convertFromFieldModel(fieldModel));
                }

                final DescriptorActionApi descriptorApi = descriptorActionApi.get();
                final FieldAccessor fieldAccessor = new FieldAccessor(fields);
                final TestConfigModel testConfig = descriptorApi.createTestConfigModel(channelFieldModel.getId(), fieldAccessor, destination);
                descriptorApi.testConfig(testConfig);
                return "Successfully sent test message.";
            } else {
                logger.error("Descriptor action api did not exist: {}", channelFieldModel.getDescriptorName());
                return "Internal server error. Failed to send test message.";
            }
        }
        return "No field model of type channel was was sent to test.";
    }

    private JobFieldModel readJobConfiguration(final ConfigurationJobModel groupedConfiguration) throws AlertDatabaseConstraintException {
        final Set<ConfigurationModel> configurations = groupedConfiguration.getCopyOfConfigurations();
        final Set<FieldModel> constructedFieldModels = new HashSet<>();
        for (final ConfigurationModel configurationModel : configurations) {
            constructedFieldModels.add(fieldModelProcessor.performReadAction(configurationModel));
        }
        return new JobFieldModel(groupedConfiguration.getJobId().toString(), constructedFieldModels);
    }

    private JobFieldModel convertToJobFieldModel(final ConfigurationJobModel configurationJobModel) throws AlertDatabaseConstraintException {
        final Set<FieldModel> constructedFieldModels = new HashSet<>();
        for (final ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
            constructedFieldModels.add(fieldModelProcessor.convertToFieldModel(configurationModel));
        }
        return new JobFieldModel(configurationJobModel.getJobId().toString(), constructedFieldModels);
    }

}
