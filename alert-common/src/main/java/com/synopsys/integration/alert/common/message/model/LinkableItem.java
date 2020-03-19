/**
 * alert-common
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
package com.synopsys.integration.alert.common.message.model;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class LinkableItem extends AlertSerializableModel implements Comparable<LinkableItem>, Summarizable {
    private static final List<String> EXCLUDED_FIELDS = List.of("collapsible", "countable", "isNumericValue", "summarizable", "isPartOfKey");

    private final String name;
    private final String value;
    private final String url;

    private boolean collapsible;
    private boolean countable;
    private boolean isNumericValue;
    private boolean summarizable;
    private boolean isPartOfKey;

    public LinkableItem(final String name, final String value) {
        this(name, value, null);
    }

    public LinkableItem(final String name, final String value, final String url) {
        this.name = name;
        this.value = value;
        this.url = url;
        this.collapsible = false;
        this.countable = false;
        this.isNumericValue = false;
        this.summarizable = false;
        this.isPartOfKey = false;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Optional<String> getUrl() {
        if (StringUtils.isNotBlank(url)) {
            return Optional.of(url);
        }
        return Optional.empty();
    }

    @Override
    public boolean isCollapsible() {
        return collapsible;
    }

    public void setCollapsible(final boolean collapsible) {
        this.collapsible = collapsible;
    }

    @Override
    public boolean isCountable() {
        return countable;
    }

    public void setCountable(final boolean countable) {
        this.countable = countable;
    }

    @Override
    public boolean isNumericValue() {
        return isNumericValue;
    }

    public void setNumericValueFlag(final boolean isNumericValue) {
        this.isNumericValue = isNumericValue;
    }

    @Override
    public boolean isSummarizable() {
        return summarizable;
    }

    public void setSummarizable(final boolean summarizable) {
        this.summarizable = summarizable;
    }

    public boolean isPartOfKey() {
        return isPartOfKey;
    }

    public void setPartOfKey(final boolean partOfKey) {
        isPartOfKey = partOfKey;
    }

    @Override
    public int compareTo(final LinkableItem otherItem) {
        if (!this.getName().equals(otherItem.getName())) {
            if (!this.isCollapsible() && otherItem.isCollapsible()) {
                return -1;
            } else if (this.isCollapsible() && !otherItem.isCollapsible()) {
                return 1;
            } else if (this.isPartOfKey() && !otherItem.isPartOfKey()) {
                return -1;
            } else if (!this.isPartOfKey() && otherItem.isPartOfKey()) {
                return 1;
            }
        }
        return CompareToBuilder.reflectionCompare(this, otherItem, EXCLUDED_FIELDS);
    }

}
