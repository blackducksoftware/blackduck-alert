/**
 * channel
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public final class AzureBoardsSearchPropertiesUtils {
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

    private AzureBoardsSearchPropertiesUtils() {
        // This class should not be instantiated
    }

}
