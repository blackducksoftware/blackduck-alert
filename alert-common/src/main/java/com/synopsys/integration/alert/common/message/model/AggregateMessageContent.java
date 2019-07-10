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

import java.util.Date;
import java.util.Optional;
import java.util.Set;

public class AggregateMessageContent extends LinkableItem {
    private final LinkableItem subTopic;
    private final MessageContentKey messageContentKey;
    private Set<CategoryItem> categoryItems;
    private Date providerCreationTime;

    public AggregateMessageContent(final String name, final String value, final Set<CategoryItem> categoryItems, Date providerCreationTime) {
        super(name, value);
        this.subTopic = null;
        this.categoryItems = categoryItems;
        this.messageContentKey = MessageContentKey.from(name, value);
        this.providerCreationTime = providerCreationTime;
    }

    public AggregateMessageContent(final String name, final String value, final String url, final Set<CategoryItem> categoryItems, Date providerCreationTime) {
        super(name, value, url);
        this.subTopic = null;
        this.categoryItems = categoryItems;
        this.messageContentKey = MessageContentKey.from(name, value);
        this.providerCreationTime = providerCreationTime;
    }

    public AggregateMessageContent(final String name, final String value, final String url, final LinkableItem subTopic, final Set<CategoryItem> categoryItems, Date providerCreationTime) {
        super(name, value, url);
        this.subTopic = subTopic;
        this.categoryItems = categoryItems;
        if (null == subTopic) {
            this.messageContentKey = MessageContentKey.from(name, value);
        } else {
            this.messageContentKey = MessageContentKey.from(name, value, subTopic.getName(), subTopic.getValue());
        }
        this.providerCreationTime = providerCreationTime;
    }

    public Optional<LinkableItem> getSubTopic() {
        return Optional.ofNullable(subTopic);
    }

    public MessageContentKey getKey() {
        return messageContentKey;
    }

    public Set<CategoryItem> getCategoryItems() {
        return categoryItems;
    }

    public void setCategoryItems(final Set<CategoryItem> categoryItems) {
        this.categoryItems = categoryItems;
    }

    public Date getProviderCreationTime() {
        return providerCreationTime;
    }
}
