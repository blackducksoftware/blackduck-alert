/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface DescriptorAccessor {

    List<RegisteredDescriptorModel> getRegisteredDescriptors() throws AlertDatabaseConstraintException;

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorByKey(DescriptorKey descriptorKey) throws AlertDatabaseConstraintException;

    List<RegisteredDescriptorModel> getRegisteredDescriptorsByType(DescriptorType descriptorType) throws AlertDatabaseConstraintException;

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(Long descriptorId) throws AlertDatabaseConstraintException;

    List<DefinedFieldModel> getFieldsForDescriptor(DescriptorKey descriptorKey, ConfigContextEnum context) throws AlertDatabaseConstraintException;

    List<DefinedFieldModel> getFieldsForDescriptorById(Long descriptorId, ConfigContextEnum context) throws AlertDatabaseConstraintException;

}
