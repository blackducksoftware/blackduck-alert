/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.metadata.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;

public class DescriptorTypesResponseModel extends AlertSerializableModel {
    // This is not a component or singleton because it is stateless. It should always be treated as static.
    public static final DescriptorTypesResponseModel DEFAULT = new DescriptorTypesResponseModel();
    public final DescriptorType[] descriptorTypes = DescriptorType.values();

    DescriptorTypesResponseModel() {
        // For serialization
    }

}
