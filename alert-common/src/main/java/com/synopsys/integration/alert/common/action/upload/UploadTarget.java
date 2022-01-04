/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action.upload;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.descriptor.config.field.validation.UploadValidationFunction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

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
