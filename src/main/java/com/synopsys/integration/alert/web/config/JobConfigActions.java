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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationJobModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.GroupedFieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JobConfigActions {
    private BaseConfigurationAccessor configurationAccessor;
    private FieldModelProcessor fieldModelProcessor;
    private ContentConverter contentConverter;

    @Autowired
    public JobConfigActions(final BaseConfigurationAccessor configurationAccessor, final FieldModelProcessor fieldModelProcessor, final ContentConverter contentConverter) {
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.contentConverter = contentConverter;
    }

    public boolean doesJobExist(UUID id) throws AlertDatabaseConstraintException {
        return null != id && configurationAccessor.getJobById(id).isPresent();
    }

    public Optional<GroupedFieldModel> getJobById(UUID id) throws AlertDatabaseConstraintException {
        final Optional<ConfigurationJobModel> jobConfiguration = configurationAccessor.getJobById(id);
        if (jobConfiguration.isPresent()) {
            final GroupedFieldModel groupedFieldModel = readGroupedConfiguration(jobConfiguration.get());
            return Optional.of(groupedFieldModel);
        }
        return Optional.empty();
    }

    public List<GroupedFieldModel> getAllJobs() throws AlertDatabaseConstraintException {
        final List<ConfigurationJobModel> allJobs = configurationAccessor.getAllJobs();
        List<GroupedFieldModel> jobFieldModels = new LinkedList<>();
        for (ConfigurationJobModel configurationJobModel : allJobs) {
            GroupedFieldModel jobFieldModel = readGroupedConfiguration(configurationJobModel);
            jobFieldModels.add(jobFieldModel);
        }
        return jobFieldModels;
    }

    public void deleteJobById(UUID id) throws AlertDatabaseConstraintException {
        final Optional<ConfigurationJobModel> jobs = configurationAccessor.getJobById(id);
        if (jobs.isPresent()) {
            ConfigurationJobModel configurationJobModel = jobs.get();
            for (ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
                fieldModelProcessor.deleteFieldModel(configurationModel);
            }
            configurationAccessor.deleteJob(configurationJobModel.getJobId());
        }
    }

    public GroupedFieldModel saveJob(GroupedFieldModel groupedFieldModel) throws AlertFieldException, AlertDatabaseConstraintException {
        validateJob(groupedFieldModel);
        Set<String> descriptorNames = new HashSet<>();
        Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
        for (FieldModel fieldModel : groupedFieldModel.getFieldModels()) {
            descriptorNames.add(fieldModel.getDescriptorName());
            final Collection<ConfigurationFieldModel> configurationFieldModelMap = fieldModelProcessor.saveFieldModel(fieldModel).values();
            configurationFieldModels.addAll(configurationFieldModelMap);
        }
        final ConfigurationJobModel savedJob = configurationAccessor.createJob(descriptorNames, configurationFieldModels);
        return convertToGroupedFieldModel(savedJob);
    }

    public GroupedFieldModel updateJob(UUID id, GroupedFieldModel groupedFieldModel) throws AlertFieldException, AlertException {
        validateJob(groupedFieldModel);
        Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
        for (FieldModel fieldModel : groupedFieldModel.getFieldModels()) {
            final Long fieldModelId = contentConverter.getLongValue(fieldModel.getId());
            final Collection<ConfigurationFieldModel> updatedFieldModels = fieldModelProcessor.updateFieldModel(fieldModelId, fieldModel);
            configurationFieldModels.addAll(updatedFieldModels);
        }
        final ConfigurationJobModel configurationJobModel = configurationAccessor.updateJob(id, configurationFieldModels);
        return convertToGroupedFieldModel(configurationJobModel);
    }

    public String validateJob(GroupedFieldModel groupedFieldModel) throws AlertFieldException {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldModel fieldModel : groupedFieldModel.getFieldModels()) {
            fieldModelProcessor.validateFieldModel(fieldModel, fieldErrors);
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public String testJob(GroupedFieldModel groupedFieldModel, String destination) throws IntegrationException {
        validateJob(groupedFieldModel);
        FieldModel channelFieldModel = null;
        for (FieldModel fieldModel : groupedFieldModel.getFieldModels()) {
            final Optional<Descriptor> descriptor = fieldModelProcessor.retrieveDescriptor(fieldModel.getDescriptorName());
            if (descriptor.filter(foundDescriptor -> DescriptorType.CHANNEL.equals(foundDescriptor.getType())).isPresent()) {
                channelFieldModel = fieldModel;
            }
        }

        if (channelFieldModel != null) {
            return fieldModelProcessor.testFieldModel(channelFieldModel, destination);
        }
        return "No field model of type channel was was sent to test.";
    }

    private GroupedFieldModel readGroupedConfiguration(ConfigurationJobModel groupedConfiguration) throws AlertDatabaseConstraintException {
        final Set<ConfigurationModel> configurations = groupedConfiguration.getCopyOfConfigurations();
        Set<FieldModel> constructedFieldModels = new HashSet<>();
        for (ConfigurationModel configurationModel : configurations) {
            constructedFieldModels.add(fieldModelProcessor.readFieldModel(configurationModel));
        }
        return new GroupedFieldModel(groupedConfiguration.getJobId().toString(), constructedFieldModels);
    }

    private GroupedFieldModel convertToGroupedFieldModel(ConfigurationJobModel configurationJobModel) throws AlertDatabaseConstraintException {
        Set<FieldModel> constructedFieldModels = new HashSet<>();
        for (ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
            constructedFieldModels.add(fieldModelProcessor.convertToFieldModel(configurationModel));
        }
        return new GroupedFieldModel(configurationJobModel.getJobId().toString(), constructedFieldModels);
    }

}
