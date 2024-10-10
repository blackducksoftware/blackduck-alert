/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;
import com.blackduck.integration.alert.common.persistence.model.DefinedFieldModel;
import com.blackduck.integration.alert.common.persistence.model.RegisteredDescriptorModel;

public interface DescriptorAccessor {
    List<RegisteredDescriptorModel> getRegisteredDescriptors();

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorByKey(DescriptorKey descriptorKey);

    List<RegisteredDescriptorModel> getRegisteredDescriptorsByType(DescriptorType descriptorType);

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(Long descriptorId);

    List<DefinedFieldModel> getFieldsForDescriptor(DescriptorKey descriptorKey, ConfigContextEnum context);

    List<DefinedFieldModel> getFieldsForDescriptorById(Long descriptorId, ConfigContextEnum context);

}
