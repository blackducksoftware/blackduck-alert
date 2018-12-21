/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.common.database;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;

public interface BaseConfigurationAccessor {

    Optional<ConfigurationModel> getConfigurationById(Long id) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorName(String descriptorName) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorType(DescriptorType descriptorType) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationByDescriptorNameAndContext(String descriptorName, ConfigContextEnum context) throws AlertDatabaseConstraintException;

    ConfigurationModel createEmptyConfiguration(String descriptorName, ConfigContextEnum context) throws AlertDatabaseConstraintException;

    ConfigurationModel createConfiguration(String descriptorName, ConfigContextEnum context, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    ConfigurationModel updateConfiguration(final Long descriptorConfigId, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    void deleteConfiguration(final Long descriptorConfigId) throws AlertDatabaseConstraintException;

    // TODO find a place for a method to pass a map of Strings to be immiediately converted to a new config

}
