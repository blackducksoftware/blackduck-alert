/**
 * alert-common
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
package com.synopsys.integration.alert.common.message.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.CompareToBuilder;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class CategoryItem extends AlertSerializableModel implements Comparable<CategoryItem> {
    private final CategoryKey categoryKey;
    private final ItemOperation operation;
    private final SortedSet<LinkableItem> items;
    private final Long notificationId;

    private static SortedSet<LinkableItem> singleton(final LinkableItem item) {
        final SortedSet<LinkableItem> sortedSet = new TreeSet<>();
        sortedSet.add(item);
        return sortedSet;
    }

    public CategoryItem(final CategoryKey categoryKey, final ItemOperation operation, final Long notificationId, final LinkableItem item) {
        this(categoryKey, operation, notificationId, singleton(item));
    }

    public CategoryItem(final CategoryKey categoryKey, final ItemOperation operation, final Long notificationId, final SortedSet<LinkableItem> items) {
        this.categoryKey = categoryKey;
        this.operation = operation;
        this.notificationId = notificationId;
        this.items = items;
    }

    public CategoryKey getCategoryKey() {
        return categoryKey;
    }

    public ItemOperation getOperation() {
        return operation;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public SortedSet<LinkableItem> getItems() {
        return items;
    }

    /**
     * Intended to be used for display purposes (such as freemarker templates).
     * @return A map from the name of a LinkableItem to all the LinkableItems with that name.
     */
    public Map<String, List<LinkableItem>> getItemsOfSameName() {
        final Map<String, List<LinkableItem>> map = new LinkedHashMap<>();
        if (null == items || items.isEmpty()) {
            return map;
        }
        for (final LinkableItem item : items) {
            final String name = item.getName();
            map.computeIfAbsent(name, ignored -> new ArrayList<>()).add(item);
        }
        return map;
    }

    @Override
    public int compareTo(final CategoryItem otherItem) {
        // If the other fields are equal the item order shouldn't matter.
        return CompareToBuilder.reflectionCompare(this, otherItem, "items");
    }

}
