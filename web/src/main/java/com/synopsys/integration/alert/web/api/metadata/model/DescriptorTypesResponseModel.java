/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.metadata.model;

import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DescriptorTypesResponseModel extends AlertSerializableModel {
    // This is not a component or singleton because it is stateless. It should always be treated as static.
    public static final DescriptorTypesResponseModel DEFAULT = new DescriptorTypesResponseModel();
    public final DescriptorType[] descriptorTypes = DescriptorType.values();

    DescriptorTypesResponseModel() {
        // For serialization
    }

}
