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
import java.util.Optional;

public class AggregateMessageContent extends LinkableItem {
    private final LinkableItem subTopic;
    private final List<CategoryItem> categoryItemList;
    private final MessageContentKey messageContentKey;

    public AggregateMessageContent(final String name, final String value, final List<CategoryItem> categoryItemList) {
        super(name, value);
        this.subTopic = null;
        this.categoryItemList = categoryItemList;
        this.messageContentKey = MessageContentKey.from(name, value);
    }

    public AggregateMessageContent(final String name, final String value, final String url, final List<CategoryItem> categoryItemList) {
        super(name, value, url);
        this.subTopic = null;
        this.categoryItemList = categoryItemList;
        this.messageContentKey = MessageContentKey.from(name, value);
    }

    public AggregateMessageContent(final String name, final String value, final String url, final LinkableItem subTopic, final List<CategoryItem> categoryItemList) {
        super(name, value, url);
        this.subTopic = subTopic;
        this.categoryItemList = categoryItemList;
        this.messageContentKey = MessageContentKey.from(name, value, subTopic.getName(), subTopic.getValue());
    }

    public Optional<LinkableItem> getSubTopic() {
        return Optional.ofNullable(subTopic);
    }

    public List<CategoryItem> getCategoryItemList() {
        return categoryItemList;
    }

    public MessageContentKey getKey() {
        return messageContentKey;
    }
}
