/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public final class AzureBoardsSearchPropertiesUtils {
    public static final String URL_DELIMITER = "|";
    public static final String LINKABLE_ITEM_DELIMITER = ":";

    private static final int MAX_KEY_LENGTH = 128;

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

        String truncatedLabel = StringUtils.truncate(linkableItem.getLabel(), MAX_KEY_LENGTH - LINKABLE_ITEM_DELIMITER.length());

        StringBuilder linkableItemBuilder = new StringBuilder();
        linkableItemBuilder.append(truncatedLabel);
        linkableItemBuilder.append(LINKABLE_ITEM_DELIMITER);
        linkableItemBuilder.append(linkableItem.getValue());
        linkableItem.getUrl()
            .ifPresent(url -> {
                linkableItemBuilder.append(URL_DELIMITER);
                linkableItemBuilder.append(url);
            });

        // Per this issue https://github.com/MicrosoftDocs/azure-devops-docs/issues/5890, it appears that we want to limit field content size to 256.
        return StringUtils.truncate(linkableItemBuilder.toString(), MAX_KEY_LENGTH);
    }

    private AzureBoardsSearchPropertiesUtils() {
        // This class should not be instantiated
    }

}
