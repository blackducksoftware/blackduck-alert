/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;

public final class RegisteredDescriptorModel extends AlertSerializableModel {
    private final Long id;
    private final String name;
    private final DescriptorType type;

    public RegisteredDescriptorModel(Long registeredDescriptorId, String registeredDescriptorName, String registeredDescriptorType) {
        this(registeredDescriptorId, registeredDescriptorName, DescriptorType.valueOf(registeredDescriptorType));
    }

    private RegisteredDescriptorModel(Long registeredDescriptorId, String registeredDescriptorName, DescriptorType registeredDescriptorType) {
        id = registeredDescriptorId;
        name = registeredDescriptorName;
        type = registeredDescriptorType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DescriptorType getType() {
        return type;
    }

}
