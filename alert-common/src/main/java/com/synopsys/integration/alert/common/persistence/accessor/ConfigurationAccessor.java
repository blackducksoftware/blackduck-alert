/**
 * alert-common
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
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

public interface ConfigurationAccessor {
    List<ConfigurationJobModel> getAllJobs();

    List<ConfigurationJobModel> getMatchingEnabledJobs(String frequency, String providerConfigName, String notificationType);

    List<ConfigurationJobModel> getMatchingEnabledJobs(String providerConfigName, String notificationType);

    Optional<ConfigurationJobModel> getJobById(UUID jobId) throws AlertDatabaseConstraintException;

    List<ConfigurationJobModel> getJobsByFrequency(FrequencyType frequency);

    ConfigurationJobModel createJob(Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    ConfigurationJobModel updateJob(UUID jobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    void deleteJob(UUID jobId) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getChannelConfigurationsByFrequency(FrequencyType frequencyType) throws AlertDatabaseConstraintException;

    Optional<ConfigurationModel> getProviderConfigurationByName(String providerConfigName) throws AlertDatabaseConstraintException;

    Optional<ConfigurationModel> getConfigurationById(Long id) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorKey(DescriptorKey descriptorKey) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorType(DescriptorType descriptorType) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorNameAndContext(String descriptorName, ConfigContextEnum context) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorKeyAndContext(DescriptorKey descriptorKey, ConfigContextEnum context) throws AlertDatabaseConstraintException;

    ConfigurationModel createConfiguration(DescriptorKey descriptorKey, ConfigContextEnum context, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    ConfigurationModel updateConfiguration(Long descriptorConfigId, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    void deleteConfiguration(ConfigurationModel configModel) throws AlertDatabaseConstraintException;

    void deleteConfiguration(Long descriptorConfigId) throws AlertDatabaseConstraintException;

}
