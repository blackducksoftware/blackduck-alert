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

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

public interface ConfigurationAccessor {
    List<ConfigurationJobModel> getAllJobs();

    Optional<ConfigurationJobModel> getJobById(final UUID jobId) throws AlertDatabaseConstraintException;

    ConfigurationJobModel createJob(final Collection<String> descriptorNames, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    ConfigurationJobModel updateJob(final UUID jobId, final Collection<String> descriptorNames, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    void deleteJob(final UUID jobId) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getChannelConfigurationsByFrequency(final FrequencyType frequencyType) throws AlertDatabaseConstraintException;

    Optional<ConfigurationModel> getConfigurationById(final Long id) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorName(final String descriptorName) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorType(final DescriptorType descriptorType) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationByDescriptorNameAndContext(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException;

    ConfigurationModel createEmptyConfiguration(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException;

    ConfigurationModel createConfiguration(final String descriptorName, final ConfigContextEnum context, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    ConfigurationModel updateConfiguration(final Long descriptorConfigId, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    void deleteConfiguration(final ConfigurationModel configModel) throws AlertDatabaseConstraintException;

    void deleteConfiguration(final Long descriptorConfigId) throws AlertDatabaseConstraintException;

}
