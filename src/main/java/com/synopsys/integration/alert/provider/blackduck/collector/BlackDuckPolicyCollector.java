/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;

public abstract class BlackDuckPolicyCollector extends BlackDuckCollector {
    public static final String CATEGORY_TYPE = "policy";

    public BlackDuckPolicyCollector(final JsonExtractor jsonExtractor, final Collection<ProviderContentType> contentTypes, final BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, contentTypes, blackDuckProperties);
    }

    protected void addApplicableItems(final SortedSet<CategoryItem> categoryItems, final Long notificationId, final Set<LinkableItem> policyItems, final ItemOperation operation, final Set<LinkableItem> applicableItems,
        SortedSet<String> keyItems) {
        final List<String> categoryKeyParts = new ArrayList<>(keyItems);
        final CategoryKey categoryKey = CategoryKey.from(CATEGORY_TYPE, categoryKeyParts);

        updatePolicyItems(policyItems);
        for (final LinkableItem item : applicableItems) {
            final SortedSet<LinkableItem> linkableItems = new TreeSet<>();
            linkableItems.add(item);
            linkableItems.addAll(policyItems);
            addItem(categoryItems, new CategoryItem(categoryKey, operation, notificationId, linkableItems));
        }
    }

    private void updatePolicyItems(final Set<LinkableItem> policyItems) {
        policyItems.forEach(this::updatePolicyItem);
    }

    private void updatePolicyItem(final LinkableItem policyItem) {
        policyItem.setCollapsible(true);
        policyItem.setCountable(true);
        policyItem.setSummarizable(true);
    }

}
