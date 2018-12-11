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
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor.RegisteredDescriptorModel;

public interface BaseDescriptorAccessor {

    List<RegisteredDescriptorModel> getRegisteredDescriptors();

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorByName(final String descriptorName) throws AlertDatabaseConstraintException;

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(final Long descriptorId) throws AlertDatabaseConstraintException;

    boolean registerDescriptorWithoutFields(final String descriptorName) throws AlertDatabaseConstraintException;

    boolean registerDescriptor(final String descriptorName, final Collection<DefinedFieldModel> descriptorFields) throws AlertDatabaseConstraintException;

    boolean unregisterDescriptor(final String descriptorName) throws AlertDatabaseConstraintException;

    List<DefinedFieldModel> getFieldsForDescriptor(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException;

    List<DefinedFieldModel> getFieldsForDescriptorById(final Long descriptorId, final ConfigContextEnum context) throws AlertDatabaseConstraintException;

    DefinedFieldModel addDescriptorField(final Long descriptorId, final DefinedFieldModel descriptorField) throws AlertDatabaseConstraintException;

    DefinedFieldModel updateFieldKey(final String oldKey, final String newKey) throws AlertDatabaseConstraintException;

    boolean deleteDescriptorField(final Long descriptorId, final DefinedFieldModel descriptorField) throws AlertDatabaseConstraintException;

    boolean deleteDescriptorField(final Long descriptorId, final String fieldKey) throws AlertDatabaseConstraintException;
}
