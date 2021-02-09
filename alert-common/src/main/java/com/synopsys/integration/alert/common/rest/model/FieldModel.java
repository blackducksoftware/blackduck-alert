/**
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
package com.synopsys.integration.alert.common.rest.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class FieldModel extends Config {
    private Map<String, FieldValueModel> keyToValues;
    private final String descriptorName;
    private final String context;
    private final String createdAt;
    private final String lastUpdated;

    private FieldModel() {
        this(null, null, null, null, null);
    }

    public FieldModel(String descriptorName, String context, Map<String, FieldValueModel> keyToValues) {
        this(null, descriptorName, context, null, null, keyToValues);
    }

    public FieldModel(String configId, String descriptorName, String context, Map<String, FieldValueModel> keyToValues) {
        this(configId, descriptorName, context, null, null, keyToValues);
    }

    public FieldModel(String descriptorName, String context, String createdAt, String lastUpdated, Map<String, FieldValueModel> keyToValues) {
        this(null, descriptorName, context, createdAt, lastUpdated, keyToValues);
    }

    public FieldModel(String configId, String descriptorName, String context, String createdAt, String lastUpdated, Map<String, FieldValueModel> keyToValues) {
        super(configId);
        this.descriptorName = descriptorName;
        this.context = context;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.keyToValues = keyToValues;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public String getContext() {
        return context;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public Map<String, FieldValueModel> getKeyToValues() {
        return keyToValues;
    }

    public void setKeyToValues(final Map<String, FieldValueModel> keyToValues) {
        this.keyToValues = keyToValues;
    }

    public Optional<FieldValueModel> getFieldValueModel(final String key) {
        return Optional.ofNullable(keyToValues.get(key));
    }

    public Optional<String> getFieldValue(final String key) {
        return getFieldValueModel(key).flatMap(FieldValueModel::getValue);
    }

    public void putField(final String key, final FieldValueModel field) {
        keyToValues.put(key, field);
    }

    public void removeField(final String key) {
        keyToValues.remove(key);
    }

    public FieldModel fill(final FieldModel fieldModel) {
        final Map<String, FieldValueModel> fieldValueModelMap = new HashMap<>();
        fieldValueModelMap.putAll(getKeyToValues());
        final Map<String, FieldValueModel> fieldsToAdd = fieldModel.getKeyToValues();
        for (final Map.Entry<String, FieldValueModel> entry : fieldsToAdd.entrySet()) {
            final String key = entry.getKey();
            if (!fieldValueModelMap.containsKey(key) || fieldValueModelMap.get(key).getValue().isEmpty()) {
                fieldValueModelMap.put(key, entry.getValue());
            }
        }
        final String modelDescriptorName = StringUtils.isNotBlank(getDescriptorName()) ? getDescriptorName() : fieldModel.getDescriptorName();
        final String modelContext = StringUtils.isNotBlank(getContext()) ? getContext() : fieldModel.getContext();
        final String modelCreatedAt = StringUtils.isNotBlank(getCreatedAt()) ? getCreatedAt() : fieldModel.getCreatedAt();
        final String modelLastUpdated = StringUtils.isNotBlank(getLastUpdated()) ? getLastUpdated() : fieldModel.getLastUpdated();
        final FieldModel newFieldModel = new FieldModel(modelDescriptorName, modelContext, modelCreatedAt, modelLastUpdated, fieldValueModelMap);
        final String id = StringUtils.isNotBlank(getId()) ? getId() : fieldModel.getId();
        newFieldModel.setId(id);
        return newFieldModel;
    }

}
