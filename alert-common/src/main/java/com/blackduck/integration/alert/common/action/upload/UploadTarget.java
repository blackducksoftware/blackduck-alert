/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.action.upload;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.descriptor.config.field.validation.UploadValidationFunction;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;

public class UploadTarget {
    private final String fieldKey;
    private final ConfigContextEnum context;
    private final DescriptorKey descriptorKey;
    private final String filename;
    private final UploadValidationFunction validationFunction;

    public UploadTarget(String fieldKey, ConfigContextEnum context, DescriptorKey descriptorKey, String filename, @Nullable UploadValidationFunction validationFunction) {
        this.fieldKey = fieldKey;
        this.context = context;
        this.descriptorKey = descriptorKey;
        this.filename = filename;
        this.validationFunction = validationFunction;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public ConfigContextEnum getContext() {
        return context;
    }

    public DescriptorKey getDescriptorKey() {
        return descriptorKey;
    }

    public String getFilename() {
        return filename;
    }

    public Optional<UploadValidationFunction> getValidationFunction() {
        return Optional.ofNullable(validationFunction);
    }

}
