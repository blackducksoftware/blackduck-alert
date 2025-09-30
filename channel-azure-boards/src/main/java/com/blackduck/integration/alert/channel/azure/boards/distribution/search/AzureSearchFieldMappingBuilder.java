/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureSearchFieldMappingBuilder {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, String> customFieldMapping = new HashMap<>();

    public static AzureSearchFieldMappingBuilder create() {
        return new AzureSearchFieldMappingBuilder();
    }

    private AzureSearchFieldMappingBuilder() {}

    public List<ReferenceToValue> buildAsList() {
        return customFieldMapping.entrySet().stream().map(ReferenceToValue::new).collect(Collectors.toList());
    }

    public AzureSearchFieldMappingBuilder addSubTopic(String subTopic) {
        addOrRemoveNullValueMapping(AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopic);
        return this;
    }

    public AzureSearchFieldMappingBuilder addComponentKey(String componentKey) {
        addOrRemoveNullValueMapping(AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, componentKey);
        return this;
    }

    public AzureSearchFieldMappingBuilder addSubComponentKey(String subComponentKey) {
        addOrRemoveNullValueMapping(AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, subComponentKey);
        return this;
    }

    public AzureSearchFieldMappingBuilder removeSubComponentKey() {
        addOrRemoveNullValueMapping(AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, null);
        return this;
    }

    public AzureSearchFieldMappingBuilder addAdditionalInfoKey(String additionalInfo) {
        addOrRemoveNullValueMapping(AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfo);
        return this;
    }

    public AzureSearchFieldMappingBuilder addCategoryKey(String categoryKey) {
        addOrRemoveNullValueMapping(AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, categoryKey);
        return this;
    }

    private void addOrRemoveNullValueMapping(String key, String value) {
        String shortenedKeyValue = StringUtils.truncate(value, AzureBoardsSearchPropertiesUtils.MAX_STRING_VALUE_LENGTH);
        // a map does not permit a null value so if the shortened key value is null then remove the key from the map.
        if(null != shortenedKeyValue) {
            customFieldMapping.put(key, shortenedKeyValue);
        } else {
            logger.debug("Removing Azure boards search field mapping: {}", key);
            customFieldMapping.remove(key);
        }
    }

    class ReferenceToValue {
        private final String referenceKey;
        private final String fieldValue;

        public ReferenceToValue(Map.Entry<String, String> pair) {
            this(pair.getKey(), pair.getValue());
        }

        public ReferenceToValue(String referenceKey, String fieldValue) {
            this.referenceKey = referenceKey;
            this.fieldValue = fieldValue;
        }

        public String getReferenceKey() {
            return referenceKey;
        }

        public String getFieldValue() {
            return fieldValue;
        }
    }
}
