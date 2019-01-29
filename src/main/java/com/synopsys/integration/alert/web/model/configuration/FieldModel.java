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
package com.synopsys.integration.alert.web.model.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.web.model.Config;

public class FieldModel extends Config {
    private final Map<String, FieldValueModel> keyToValues;
    private final String descriptorName;
    private final String context;

    private FieldModel() {
        this(null, null, null);
    }

    public FieldModel(final String descriptorName, final String context, final Map<String, FieldValueModel> keyToValues) {
        this.descriptorName = descriptorName;
        this.context = context;
        this.keyToValues = keyToValues;
    }

    public FieldModel(final String configId, final String descriptorName, final String context, final Map<String, FieldValueModel> keyToValues) {
        super(configId);
        this.descriptorName = descriptorName;
        this.context = context;
        this.keyToValues = keyToValues;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public String getContext() {
        return context;
    }

    public Map<String, FieldValueModel> getKeyToValues() {
        return keyToValues;
    }

    public Optional<FieldValueModel> getField(final String key) {
        return Optional.ofNullable(keyToValues.get(key));
    }

    public Optional<String> getFieldValue(final String key) {
        return getField(key).flatMap(fieldValueModel -> fieldValueModel.getValue());
    }

    public Collection<String> getFieldValues(final String key) {
        return getField(key).map(fieldValueModel -> fieldValueModel.getValues()).orElse(List.of());
    }

    public void putField(final String key, final FieldValueModel field) {
        keyToValues.put(key, field);
    }

    public FieldModel combine(final FieldModel fieldModel) {
        String id = StringUtils.isNotBlank(getId()) ? getId() : fieldModel.getId();
        String descriptorName = StringUtils.isNotBlank(getDescriptorName()) ? getDescriptorName() : fieldModel.getDescriptorName();
        String context = StringUtils.isNotBlank(getContext()) ? getContext() : fieldModel.getContext();
        final Map<String, FieldValueModel> fieldValueModelMap = new HashMap<>();
        fieldValueModelMap.putAll(this.getKeyToValues());
        fieldValueModelMap.putAll(fieldModel.getKeyToValues());
        final FieldModel newFieldModel = new FieldModel(descriptorName, context, fieldValueModelMap);
        newFieldModel.setId(id);
        return newFieldModel;
    }

}
