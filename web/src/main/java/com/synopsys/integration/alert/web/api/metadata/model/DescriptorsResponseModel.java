/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.metadata.model;

import java.util.Set;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;

public class DescriptorsResponseModel extends AlertSerializableModel {
    private final Set<DescriptorMetadata> descriptors;

    public DescriptorsResponseModel() {
        // For serialization
        this.descriptors = Set.of();
    }

    public DescriptorsResponseModel(Set<DescriptorMetadata> descriptors) {
        this.descriptors = descriptors;
    }

    public Set<DescriptorMetadata> getDescriptors() {
        return descriptors;
    }

}
