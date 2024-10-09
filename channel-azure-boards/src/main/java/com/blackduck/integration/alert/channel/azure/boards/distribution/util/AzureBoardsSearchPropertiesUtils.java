package com.blackduck.integration.alert.channel.azure.boards.distribution.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.common.message.model.LinkableItem;

public final class AzureBoardsSearchPropertiesUtils {
    public static final String URL_DELIMITER = "|";
    public static final String LINKABLE_ITEM_DELIMITER = ":";

    // String field value length (type - String, label values are 255 char limit): https://learn.microsoft.com/en-us/azure/devops/reference/xml/field-definition-element-reference?view=azure-devops-2022.
    public static final int MAX_STRING_VALUE_LENGTH = 255;

    public static String createProviderKey(String providerName, String providerUrl) {
        StringBuilder providerKeyBuilder = new StringBuilder();
        providerKeyBuilder.append(providerName);
        providerKeyBuilder.append(URL_DELIMITER);
        providerKeyBuilder.append(providerUrl);
        return providerKeyBuilder.toString();
    }

    public static String createNullableLinkableItemKey(@Nullable LinkableItem linkableItem) {
        if (null == linkableItem) {
            return null;
        }

        StringBuilder linkableItemBuilder = new StringBuilder();
        linkableItemBuilder.append(linkableItem.getLabel());
        linkableItemBuilder.append(LINKABLE_ITEM_DELIMITER);
        // Single quotes need to be escaped with an additional single quote for Azure OData queries
        linkableItemBuilder.append(linkableItem.getValue().replace("'", "''"));

        linkableItem.getUrl()
            .ifPresent(url -> {
                linkableItemBuilder.append(URL_DELIMITER);
                linkableItemBuilder.append(url);
            });

        // Truncating to a valid amount
        return StringUtils.truncate(linkableItemBuilder.toString(), MAX_STRING_VALUE_LENGTH);
    }

    private AzureBoardsSearchPropertiesUtils() {
        // This class should not be instantiated
    }

}
