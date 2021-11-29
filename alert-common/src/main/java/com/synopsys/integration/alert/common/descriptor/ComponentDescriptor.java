/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor;

import java.util.Optional;

import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public abstract class ComponentDescriptor extends Descriptor {
    public ComponentDescriptor(DescriptorKey descriptorKey, UIConfig componentUIConfig) {
        super(descriptorKey, DescriptorType.COMPONENT);
        addGlobalUiConfig(componentUIConfig);
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.empty();
    }
}
