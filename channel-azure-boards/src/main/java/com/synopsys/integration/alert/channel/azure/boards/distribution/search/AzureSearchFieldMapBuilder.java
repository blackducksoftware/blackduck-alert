/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class AzureSearchFieldMapBuilder {
    private static final int AZURE_CUSTOM_PROPERTY_LIMIT = 256;

    private final Map<String, String> customFieldMapping = new HashMap<>();

    public static AzureSearchFieldMapBuilder create() {
        return new AzureSearchFieldMapBuilder();
    }

    private AzureSearchFieldMapBuilder() {}

    public Map<String, String> build() {
        return customFieldMapping;
    }

    public List<ReferenceToValue> buildAsList() {
        return customFieldMapping.entrySet().stream().map(ReferenceToValue::new).collect(Collectors.toList());
    }

    public AzureSearchFieldMapBuilder addSubTopic(String subTopic) {
        addMapping(AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopic);
        return this;
    }

    public AzureSearchFieldMapBuilder addComponentKey(String componentKey) {
        addMapping(AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, componentKey);
        return this;
    }

    public AzureSearchFieldMapBuilder addSubComponentKey(String subComponentKey) {
        addMapping(AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, subComponentKey);
        return this;
    }

    public AzureSearchFieldMapBuilder addAdditionInfoKey(String additionalInfo) {
        addMapping(AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfo);
        return this;
    }

    public AzureSearchFieldMapBuilder addCategoryKey(String categoryKey) {
        addMapping(AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, categoryKey);
        return this;
    }

    private void addMapping(String key, String value) {
        String shortenedKeyValue = StringUtils.truncate(value, AzureSearchFieldMapBuilder.AZURE_CUSTOM_PROPERTY_LIMIT);
        customFieldMapping.put(key, shortenedKeyValue);
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
