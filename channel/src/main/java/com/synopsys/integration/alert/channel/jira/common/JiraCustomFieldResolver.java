/**
 * channel
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
package com.synopsys.integration.alert.channel.jira.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.jira.common.model.CustomFieldDefinitionModel;
import com.synopsys.integration.alert.channel.jira.common.model.JiraCustomFieldConfig;
import com.synopsys.integration.alert.channel.jira.common.model.JiraResolvedCustomField;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingSupplier;
import com.synopsys.integration.jira.common.model.response.CustomFieldCreationResponseModel;

public class JiraCustomFieldResolver {
    private static final String CUSTOM_FIELD_TYPE_STRING_VALUE = "string";
    private static final String CUSTOM_FIELD_TYPE_ARRAY_VALUE = "array";
    private static final String CUSTOM_FIELD_TYPE_OPTION_VALUE = "option";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ThrowingSupplier<List<CustomFieldCreationResponseModel>, IntegrationException> retrieveAvailableFields;
    private final Map<String, CustomFieldCreationResponseModel> fieldCache;
    private boolean isCachePopulated;

    public JiraCustomFieldResolver(ThrowingSupplier<List<CustomFieldCreationResponseModel>, IntegrationException> retrieveAvailableFields) {
        this.retrieveAvailableFields = retrieveAvailableFields;
        this.fieldCache = new HashMap<>();
        this.isCachePopulated = false;
    }

    public final JiraResolvedCustomField resolveCustomField(JiraCustomFieldConfig jiraCustomFieldConfig) {
        CustomFieldDefinitionModel fieldDefinition = retrieveCustomFieldDefinition(jiraCustomFieldConfig);
        Object requestObject = convertValueToRequestObject(fieldDefinition, jiraCustomFieldConfig);
        return new JiraResolvedCustomField(fieldDefinition.getFieldId(), requestObject);
    }

    protected Optional<CustomFieldCreationResponseModel> retrieveFieldDefinition(String fieldName) {
        if (!isCachePopulated) {
            try {
                initializeCache();
            } catch (IntegrationException e) {
                logger.warn("No Jira Cloud user-visible fields found");
                return Optional.empty();
            }
        }
        return Optional.ofNullable(fieldCache.get(fieldName));
    }

    protected CustomFieldDefinitionModel retrieveCustomFieldDefinition(JiraCustomFieldConfig customFieldConfig) {
        String fieldName = customFieldConfig.getFieldName();
        CustomFieldCreationResponseModel fieldResponse = retrieveFieldDefinition(fieldName)
                                                             .orElseThrow(() -> new AlertRuntimeException(String.format("No custom field named '%s' existed", fieldName)));
        return new CustomFieldDefinitionModel(fieldResponse.getId(), fieldResponse.getSchema().getType());
    }

    protected Object convertValueToRequestObject(CustomFieldDefinitionModel fieldDefinition, JiraCustomFieldConfig jiraCustomFieldConfig) {
        String fieldType = fieldDefinition.getFieldType();
        String innerFieldValue = extractUsableInnerValue(jiraCustomFieldConfig);
        switch (fieldType) {
            case CUSTOM_FIELD_TYPE_STRING_VALUE:
                return jiraCustomFieldConfig;
            case CUSTOM_FIELD_TYPE_ARRAY_VALUE:
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(innerFieldValue);
                return jsonArray;
            case CUSTOM_FIELD_TYPE_OPTION_VALUE:
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("value", innerFieldValue);
                return jsonObject;
            default:
                throw new AlertRuntimeException(String.format("Unsupported field type '%s' for field: %s", fieldType, jiraCustomFieldConfig.getFieldName()));
        }
    }

    private String extractUsableInnerValue(JiraCustomFieldConfig jiraCustomFieldConfig) {
        return jiraCustomFieldConfig.getFieldReplacementValue().orElseGet(jiraCustomFieldConfig::getFieldOriginalValue);
    }

    private void initializeCache() throws IntegrationException {
        List<CustomFieldCreationResponseModel> userVisibleFields = retrieveAvailableFields.get();
        for (CustomFieldCreationResponseModel fieldModel : userVisibleFields) {
            fieldCache.put(fieldModel.getName(), fieldModel);
        }
        isCachePopulated = true;
    }

}
