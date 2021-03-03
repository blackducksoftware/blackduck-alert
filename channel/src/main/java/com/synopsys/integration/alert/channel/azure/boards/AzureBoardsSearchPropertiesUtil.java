/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class AzureBoardsSearchPropertiesUtil {
    private static final char URL_DELIMITER = '|';

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
        linkableItemBuilder.append(linkableItem.getName());
        linkableItemBuilder.append(':');
        linkableItemBuilder.append(linkableItem.getValue());
        linkableItem.getUrl()
            .ifPresent(url -> {
                linkableItemBuilder.append(URL_DELIMITER);
                linkableItemBuilder.append(url);
            });
        return linkableItemBuilder.toString();
    }

}
