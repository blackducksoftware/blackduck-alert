package com.blackduck.integration.alert.web.api.metadata.model;

import java.util.Set;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.descriptor.config.ui.DescriptorMetadata;

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
