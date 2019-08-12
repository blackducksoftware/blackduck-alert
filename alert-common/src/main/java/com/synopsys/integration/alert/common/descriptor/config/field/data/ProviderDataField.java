/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.descriptor.config.field.data;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigValidationFunction;

public abstract class ProviderDataField extends ConfigField {
    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive, boolean readOnly, String panel, String header,
        ConfigValidationFunction validationFunction) {
        super(key, label, description, type, required, sensitive, readOnly, panel, header, validationFunction);
    }

    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive, String panel) {
        super(key, label, description, type, required, sensitive, panel);
    }

    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive, String panel,
        ConfigValidationFunction validationFunction) {
        super(key, label, description, type, required, sensitive, panel, validationFunction);
    }

    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive) {
        super(key, label, description, type, required, sensitive);
    }

    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive,
        ConfigValidationFunction validationFunction) {
        super(key, label, description, type, required, sensitive, validationFunction);
    }

}
