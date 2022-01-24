/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingSupplier;
import com.synopsys.integration.jira.common.model.response.CustomFieldCreationResponseModel;

public class JiraCustomFieldResolver {
    private static final String CUSTOM_FIELD_TYPE_STRING_VALUE = "string";
    private static final String CUSTOM_FIELD_TYPE_ARRAY_VALUE = "array";
    private static final String CUSTOM_FIELD_TYPE_OPTION_VALUE = "option";
    private static final String CUSTOM_FIELD_TYPE_PRIORITY_VALUE = "priority";
    private static final String CUSTOM_FIELD_TYPE_USER_VALUE = "user";
    private static final String CUSTOM_FIELD_TYPE_COMPONENT_VALUE = "component";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ThrowingSupplier<List<CustomFieldCreationResponseModel>, IntegrationException> retrieveAvailableFields;
    private final Map<String, CustomFieldCreationResponseModel> nameToModelCache;
    private final Map<String, String> idToNameCache;
    private boolean cachesHaveBeenInitialized;

    public JiraCustomFieldResolver(ThrowingSupplier<List<CustomFieldCreationResponseModel>, IntegrationException> retrieveAvailableFields) {
        this.retrieveAvailableFields = retrieveAvailableFields;
        this.nameToModelCache = new HashMap<>();
        this.idToNameCache = new HashMap<>();
        this.cachesHaveBeenInitialized = false;
    }

    public final JiraResolvedCustomField resolveCustomField(JiraCustomFieldConfig jiraCustomFieldConfig) {
        CustomFieldDefinitionModel fieldDefinition = retrieveCustomFieldDefinition(jiraCustomFieldConfig);
        Object requestObject = convertValueToRequestObject(fieldDefinition, jiraCustomFieldConfig);
        return new JiraResolvedCustomField(fieldDefinition.getFieldId(), requestObject);
    }

    public Set<String> getCustomFieldIds() {
        initialize();
        return idToNameCache.keySet();
    }

    public Optional<String> resolveCustomFieldIdToName(String customFieldId) {
        initialize();
        return Optional.ofNullable(idToNameCache.get(customFieldId));
    }

    protected Optional<CustomFieldCreationResponseModel> retrieveFieldDefinition(String fieldName) {
        initialize();
        return Optional.ofNullable(nameToModelCache.get(fieldName));
    }

    protected CustomFieldDefinitionModel retrieveCustomFieldDefinition(JiraCustomFieldConfig customFieldConfig) {
        String fieldName = customFieldConfig.getFieldName();
        CustomFieldCreationResponseModel fieldResponse = retrieveFieldDefinition(fieldName)
            .orElseThrow(() -> new AlertRuntimeException(String.format("No custom field named '%s' existed", fieldName)));
        return new CustomFieldDefinitionModel(fieldResponse.getId(), fieldResponse.getSchema().getType(), fieldResponse.getSchema().getItems());
    }

    protected Object convertValueToRequestObject(CustomFieldDefinitionModel fieldDefinition, JiraCustomFieldConfig jiraCustomFieldConfig) {
        String fieldType = fieldDefinition.getFieldType();
        String innerFieldValue = extractUsableInnerValue(jiraCustomFieldConfig);
        switch (fieldType) {
            case CUSTOM_FIELD_TYPE_STRING_VALUE:
                return innerFieldValue;
            case CUSTOM_FIELD_TYPE_ARRAY_VALUE:
                return createJsonArray(innerFieldValue, fieldDefinition.getFieldArrayItems());
            case CUSTOM_FIELD_TYPE_OPTION_VALUE:
                return createJsonObject("value", innerFieldValue);
            case CUSTOM_FIELD_TYPE_PRIORITY_VALUE:
                return createJsonObject("name", innerFieldValue);
            case CUSTOM_FIELD_TYPE_USER_VALUE:
                // "name" is used for Jira Server (ignored on Jira Cloud)
                JsonObject createUserObject = createJsonObject("name", innerFieldValue);
                // "accountId" is used for Jira Cloud (ignored on Jira Server)
                createUserObject.addProperty("accountId", innerFieldValue);
                // TODO consider separating this functionality depending on which Jira channel is being used
                return createUserObject;
            default:
                throw new AlertRuntimeException(String.format("Unsupported field type '%s' for field: %s", fieldType, jiraCustomFieldConfig.getFieldName()));
        }
    }

    private void initialize() {
        if (!cachesHaveBeenInitialized) {
            try {
                initializeCaches();
            } catch (IntegrationException e) {
                logger.warn("No Jira user-visible fields found");
            }
        }
    }

    private JsonArray createJsonArray(String innerFieldValue, String fieldArrayItems) {
        JsonArray jsonArray = new JsonArray();
        List<JsonElement> elements = createJsonArrayElements(innerFieldValue, fieldArrayItems);
        for (JsonElement element : elements) {
            jsonArray.add(element);
        }
        return jsonArray;
    }

    private List<JsonElement> createJsonArrayElements(String innerFieldValue, String fieldArrayItems) {
        switch (fieldArrayItems) {
            case CUSTOM_FIELD_TYPE_STRING_VALUE:
                return List.of(new JsonPrimitive(innerFieldValue));
            case CUSTOM_FIELD_TYPE_COMPONENT_VALUE:
                return Arrays.stream(StringUtils.split(innerFieldValue))
                    .map(fieldValue -> createJsonObject("name", fieldValue))
                    .collect(Collectors.toList());
            case CUSTOM_FIELD_TYPE_OPTION_VALUE:
                return Arrays.stream(StringUtils.split(innerFieldValue))
                    .map(fieldValue -> createJsonObject("value", fieldValue))
                    .collect(Collectors.toList());
            default:
                throw new AlertRuntimeException(String.format("Unsupported item: '%s' for array field type", fieldArrayItems));
        }
    }

    private String extractUsableInnerValue(JiraCustomFieldConfig jiraCustomFieldConfig) {
        return jiraCustomFieldConfig.getFieldReplacementValue().orElseGet(jiraCustomFieldConfig::getFieldOriginalValue);
    }

    private void initializeCaches() throws IntegrationException {
        List<CustomFieldCreationResponseModel> userVisibleFields = retrieveAvailableFields.get();
        for (CustomFieldCreationResponseModel jiraField : userVisibleFields) {
            nameToModelCache.put(jiraField.getName(), jiraField);
            idToNameCache.put(jiraField.getId(), jiraField.getName());
        }
        cachesHaveBeenInitialized = true;
    }

    private JsonObject createJsonObject(String key, String value) {
        JsonObject objectWithName = new JsonObject();
        objectWithName.addProperty(key, value);
        return objectWithName;
    }

}
