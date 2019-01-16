/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;

@Component
public class FieldCreatorUtil {

    public ConfigurationFieldModel createFieldModel(final String key, final String value) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValue(value);
        return field;
    }

    public ConfigurationFieldModel createFieldModel(final String key, final Collection<String> values) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValues(values);
        return field;
    }

    public ConfigurationFieldModel createSensitiveFieldModel(final String key, final String value) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.createSensitive(key);
        field.setFieldValue(value);
        return field;
    }

    public void addFieldModel(final String key, final Object value, final List<ConfigurationFieldModel> fieldModels) {
        if (null != value) {
            fieldModels.add(createFieldModel(key, String.valueOf(value)));
        }
    }

    public void addSecureFieldModel(final String key, final Object value, final List<ConfigurationFieldModel> fieldModels) {
        if (null != value) {
            fieldModels.add(createSensitiveFieldModel(key, String.valueOf(value)));
        }
    }
}
