/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.blackduck.integration.alert.azure.boards.common.util.AzureFieldDefinition;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public final class AzureBoardsWorkItemExtractionUtils {
    private static final String UNKNOWN_VALUE = "Unknown";

    public static LinkableItem extractLinkableItem(WorkItemFieldsWrapper workItemFields, AzureFieldDefinition<String> fieldDefinition) {
        Optional<String> optionalFieldValue = workItemFields.getField(fieldDefinition);
        return optionalFieldValue
                   .map(AzureBoardsWorkItemExtractionUtils::extractLinkableItem)
                   .orElseGet(() -> new LinkableItem(UNKNOWN_VALUE, UNKNOWN_VALUE));
    }

    public static LinkableItem extractLinkableItem(String fieldValue) {
        String label = UNKNOWN_VALUE;
        String value = UNKNOWN_VALUE;
        String url = null;

        if (StringUtils.contains(fieldValue, AzureBoardsSearchPropertiesUtils.LINKABLE_ITEM_DELIMITER)) {
            label = StringUtils.substringBefore(fieldValue, AzureBoardsSearchPropertiesUtils.LINKABLE_ITEM_DELIMITER);
            value = StringUtils.substringAfter(fieldValue, AzureBoardsSearchPropertiesUtils.LINKABLE_ITEM_DELIMITER);
        }

        if (StringUtils.contains(value, AzureBoardsSearchPropertiesUtils.URL_DELIMITER)) {
            String urlCandidate = StringUtils.substringAfter(value, AzureBoardsSearchPropertiesUtils.URL_DELIMITER);
            value = StringUtils.substringBefore(fieldValue, AzureBoardsSearchPropertiesUtils.URL_DELIMITER);
            if (StringUtils.isNotBlank(urlCandidate)) {
                url = urlCandidate;
            }
        }
        return new LinkableItem(label, value, url);
    }

    private AzureBoardsWorkItemExtractionUtils() {
    }

}
