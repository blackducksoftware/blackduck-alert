/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.digest.model;

import java.util.List;

public class CategoryData {
    private final String categoryKey;

    private final List<ItemData> itemList;

    private final int itemCount;

    public CategoryData(final String categoryKey, final List<ItemData> itemList, final int itemCount) {
        this.categoryKey = categoryKey;
        this.itemList = itemList;
        this.itemCount = itemCount;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public List<ItemData> getItemList() {
        return itemList;
    }

    public int getItemCount() {
        return itemCount;
    }

    @Override
    public String toString() {
        return "CategoryData [categoryKey=" + categoryKey + ", itemList=" + itemList + ", itemCount=" + itemCount + "]";
    }
}
