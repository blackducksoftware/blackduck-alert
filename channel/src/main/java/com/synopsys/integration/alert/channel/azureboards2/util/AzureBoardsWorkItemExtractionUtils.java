/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azureboards2.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public final class AzureBoardsWorkItemExtractionUtils {
    public static LinkableItem extractLinkableItem(WorkItemFieldsWrapper workItemFields, AzureFieldDefinition<String> fieldDefinition) {
        Optional<String> optionalFieldValue = workItemFields.getField(fieldDefinition);
        return optionalFieldValue
                   .map(AzureBoardsWorkItemExtractionUtils::extractLinkableItem)
                   .orElseGet(() -> new LinkableItem("Unknown", "Unknown"));
    }

    public static LinkableItem extractLinkableItem(String fieldValue) {
        String label = "Unknown";
        String value = "Unknown";
        String url = null;

        if (StringUtils.contains(fieldValue, ':')) {
            label = StringUtils.substringBefore(fieldValue, ":");
            value = StringUtils.substringAfter(fieldValue, ":");
        }

        if (StringUtils.contains(value, '|')) {
            String urlCandidate = StringUtils.substringAfter(value, "|");
            value = StringUtils.substringBefore(fieldValue, "|");
            if (StringUtils.isNotBlank(urlCandidate)) {
                url = urlCandidate;
            }
        }
        return new LinkableItem(label, value, url);
    }

    private AzureBoardsWorkItemExtractionUtils() {
    }

}
