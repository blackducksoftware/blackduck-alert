/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.action.upload;

import java.util.Optional;

import javax.annotation.Nullable;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.UploadValidationFunction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;

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
