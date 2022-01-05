/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class FieldModel extends Config {
    private Map<String, FieldValueModel> keyToValues;
    // TODO DescriptorKey and a ConfigContextEnum should be available instead of Strings
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

    public void setKeyToValues(Map<String, FieldValueModel> keyToValues) {
        this.keyToValues = keyToValues;
    }

    public Optional<FieldValueModel> getFieldValueModel(String key) {
        return Optional.ofNullable(keyToValues.get(key));
    }

    public Optional<String> getFieldValue(String key) {
        return getFieldValueModel(key).flatMap(FieldValueModel::getValue);
    }

    public void putField(String key, FieldValueModel field) {
        keyToValues.put(key, field);
    }

    public void removeField(String key) {
        keyToValues.remove(key);
    }

    public FieldModel fill(FieldModel fieldModel) {
        Map<String, FieldValueModel> fieldValueModelMap = new HashMap<>();
        fieldValueModelMap.putAll(getKeyToValues());
        Map<String, FieldValueModel> fieldsToAdd = fieldModel.getKeyToValues();
        for (Map.Entry<String, FieldValueModel> entry : fieldsToAdd.entrySet()) {
            String key = entry.getKey();
            if (!fieldValueModelMap.containsKey(key) || fieldValueModelMap.get(key).getValue().isEmpty()) {
                fieldValueModelMap.put(key, entry.getValue());
            }
        }
        String modelDescriptorName = StringUtils.isNotBlank(getDescriptorName()) ? getDescriptorName() : fieldModel.getDescriptorName();
        String modelContext = StringUtils.isNotBlank(getContext()) ? getContext() : fieldModel.getContext();
        String modelCreatedAt = StringUtils.isNotBlank(getCreatedAt()) ? getCreatedAt() : fieldModel.getCreatedAt();
        String modelLastUpdated = StringUtils.isNotBlank(getLastUpdated()) ? getLastUpdated() : fieldModel.getLastUpdated();
        FieldModel newFieldModel = new FieldModel(modelDescriptorName, modelContext, modelCreatedAt, modelLastUpdated, fieldValueModelMap);
        String id = StringUtils.isNotBlank(getId()) ? getId() : fieldModel.getId();
        newFieldModel.setId(id);
        return newFieldModel;
    }

}
