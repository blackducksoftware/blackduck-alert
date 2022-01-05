/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperation;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class AzureSearchFieldBuilder {
    private static final int AZURE_CUSTOM_PROPERTY_LIMIT = 256;

    private final List<WorkItemElementOperationModel> customFields = new ArrayList<>(7);

    public static AzureSearchFieldBuilder create() {
        return new AzureSearchFieldBuilder();
    }

    private AzureSearchFieldBuilder() {}

    public List<WorkItemElementOperationModel> build() {
        return customFields;
    }

    public AzureSearchFieldBuilder addProviderKey(String providerKey) {
        addStringField(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, providerKey);
        return this;
    }

    public AzureSearchFieldBuilder addTopicKey(String topicKey) {
        addStringField(AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, topicKey);
        return this;
    }

    public AzureSearchFieldBuilder addSubTopicKey(String subTopicKey) {
        addStringField(AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopicKey);
        return this;
    }

    public AzureSearchFieldBuilder addComponentKey(String componentKey) {
        addStringField(AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, componentKey);
        return this;
    }

    public AzureSearchFieldBuilder addSubComponentKey(String subComponentKey) {
        addStringField(AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, subComponentKey);
        return this;
    }

    public AzureSearchFieldBuilder addAdditionalInfoKey(String additionalInfoKey) {
        addStringField(AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfoKey);
        return this;
    }

    public AzureSearchFieldBuilder addCategoryKey(String categoryKey) {
        addStringField(AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, categoryKey);
        return this;
    }

    private void addStringField(String fieldReferenceName, @Nullable String fieldValue) {
        if (null != fieldValue) {
            String shortenedValue = StringUtils.truncate(fieldValue, AzureSearchFieldBuilder.AZURE_CUSTOM_PROPERTY_LIMIT);
            AzureFieldDefinition<String> alertProviderKeyFieldDefinition = AzureFieldDefinition.stringField(fieldReferenceName);
            WorkItemElementOperationModel alertProviderKeyField = WorkItemElementOperationModel.fieldElement(WorkItemElementOperation.ADD, alertProviderKeyFieldDefinition, shortenedValue);
            customFields.add(alertProviderKeyField);
        }
    }

}
