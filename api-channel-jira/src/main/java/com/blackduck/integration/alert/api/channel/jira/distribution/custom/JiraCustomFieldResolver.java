/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.custom;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.function.ThrowingSupplier;
import com.blackduck.integration.jira.common.model.response.CustomFieldCreationResponseModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JiraCustomFieldResolver {
    private static final String CUSTOM_FIELD_TYPE_STRING_VALUE = "string";
    private static final String CUSTOM_FIELD_TYPE_ARRAY_VALUE = "array";
    private static final String CUSTOM_FIELD_TYPE_OPTION_VALUE = "option";
    private static final String CUSTOM_FIELD_TYPE_PRIORITY_VALUE = "priority";
    private static final String CUSTOM_FIELD_TYPE_USER_VALUE = "user";
    private static final String CUSTOM_FIELD_TYPE_COMPONENT_VALUE = "component";
    private static final String CUSTOM_FIELD_TYPE_OBJECT_VALUE = "object";
    private static final String CUSTOM_FIELD_TYPE_ANY_VALUE = "any";

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

        if(jiraCustomFieldConfig.isTreatValueAsJson()) {
            try {
                return JsonParser.parseString(innerFieldValue);
            } catch (JsonSyntaxException ex) {
                throw new AlertRuntimeException(String.format("Invalid JSON value for field: %s error: %s", jiraCustomFieldConfig.getFieldName(), ex.getMessage()));
            }
        } else {
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
                case CUSTOM_FIELD_TYPE_OBJECT_VALUE:
                    // This is a Jira Cloud custom field type.  It is possible to create a JSON object
                    return createJsonObjectFromString(innerFieldValue, jiraCustomFieldConfig.getFieldName());
                case CUSTOM_FIELD_TYPE_ANY_VALUE:
                    // Write the string value as is for any custom field of type any.
                    // custom fields are written to the Jira Server database as a string.
                    return innerFieldValue;
                default:
                    throw new AlertRuntimeException(String.format("Unsupported field type '%s' for field: %s", fieldType, jiraCustomFieldConfig.getFieldName()));
            }
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

    private JsonObject createJsonObjectFromString(String jsonValue, String fieldName) {
        try {
            return JsonParser.parseString(jsonValue).getAsJsonObject();
        } catch (JsonSyntaxException jse) {
            throw new AlertRuntimeException(String.format("Invalid JSON syntax for field type '%s' field: %s", CUSTOM_FIELD_TYPE_OBJECT_VALUE, fieldName));
        }
    }

}
