/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface DescriptorAccessor {
    List<RegisteredDescriptorModel> getRegisteredDescriptors();

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorByKey(DescriptorKey descriptorKey);

    List<RegisteredDescriptorModel> getRegisteredDescriptorsByType(DescriptorType descriptorType);

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(Long descriptorId);

    List<DefinedFieldModel> getFieldsForDescriptor(DescriptorKey descriptorKey, ConfigContextEnum context);

    List<DefinedFieldModel> getFieldsForDescriptorById(Long descriptorId, ConfigContextEnum context);

}
