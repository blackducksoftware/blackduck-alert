/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.common.model;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;

public class CategoryItem {
    private final CategoryKey categoryKey;
    private final ItemOperation operation;
    private final List<LinkableItem> itemList;
    private final Long notificationId;

    public CategoryItem(final CategoryKey categoryKey, final ItemOperation operation, final Long notificationId, final List<LinkableItem> itemList) {
        this.categoryKey = categoryKey;
        this.operation = operation;
        this.notificationId = notificationId;
        this.itemList = itemList;
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

    public List<LinkableItem> getItemList() {
        return itemList;
    }
}
