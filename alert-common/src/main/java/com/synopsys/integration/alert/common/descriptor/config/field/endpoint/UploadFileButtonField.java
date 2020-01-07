/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import java.util.List;

import com.synopsys.integration.alert.common.action.UploadEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class UploadFileButtonField extends EndpointField {
    private final List<String> accept;
    private final String capture;
    private final Boolean multiple;

    private UploadFileButtonField(String key, String label, String description, boolean required, String buttonLabel, List<String> accept, String capture, Boolean multiple, ConfigValidationFunction... validationFunction) {
        super(key, label, description, FieldType.UPLOAD_FILE_BUTTON, required, false, buttonLabel, UploadEndpointManager.UPLOAD_ENDPOINT_URL);
        this.accept = accept;
        this.capture = capture;
        this.multiple = multiple;
        this.setValidationFunctions(validationFunction);
    }

    private UploadFileButtonField(String key, String label, String description, boolean required, String buttonLabel, List<String> accept, String capture, Boolean multiple) {
        super(key, label, description, FieldType.UPLOAD_FILE_BUTTON, required, false, buttonLabel, UploadEndpointManager.UPLOAD_ENDPOINT_URL);
        this.accept = accept;
        this.capture = capture;
        this.multiple = multiple;
    }

    public static UploadFileButtonField create(String key, String label, String description, String buttonLabel, List<String> accept, String capture, Boolean multiple) {
        return new UploadFileButtonField(key, label, description, false, buttonLabel, accept, capture, multiple);
    }

    public static UploadFileButtonField createRequired(String key, String label, String description, String buttonLabel, List<String> accept, String capture, Boolean multiple) {
        return new UploadFileButtonField(key, label, description, true, buttonLabel, accept, capture, multiple);
    }

    public static UploadFileButtonField create(String key, String label, String description, String buttonLabel, List<String> accept, String capture, Boolean multiple, ConfigValidationFunction... validationFunction) {
        return new UploadFileButtonField(key, label, description, false, buttonLabel, accept, capture, multiple, validationFunction);
    }

    public static UploadFileButtonField createRequired(String key, String label, String description, String buttonLabel, List<String> accept, String capture, Boolean multiple, ConfigValidationFunction... validationFunction) {
        return new UploadFileButtonField(key, label, description, true, buttonLabel, accept, capture, multiple, validationFunction);
    }

    public List<String> getAccept() {
        return accept;
    }

    public String getCapture() {
        return capture;
    }

    public Boolean getMultiple() {
        return multiple;
    }
}
