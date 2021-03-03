/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.message.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class LinkableItem extends AlertSerializableModel implements Comparable<LinkableItem> {
    private static final String[] EXCLUDED_FIELDS = { "collapsible", "isNumericValue" };

    private final String name;
    private final String value;
    private final String url;

    private boolean collapsible;
    private boolean isNumericValue;

    public LinkableItem(String name, String value) {
        this(name, value, null);
    }

    public LinkableItem(String name, String value, String url) {
        this.name = name;
        this.value = value;
        this.url = url;
        this.collapsible = false;
        this.isNumericValue = false;
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

    public boolean isCollapsible() {
        return collapsible;
    }

    public void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
    }

    public boolean isNumericValue() {
        return isNumericValue;
    }

    public void setNumericValueFlag(boolean isNumericValue) {
        this.isNumericValue = isNumericValue;
    }

    @Override
    public int compareTo(LinkableItem otherItem) {
        if (!this.getName().equals(otherItem.getName())) {
            if (!this.isCollapsible() && otherItem.isCollapsible()) {
                return -1;
            } else if (this.isCollapsible() && !otherItem.isCollapsible()) {
                return 1;
            }
        }
        return CompareToBuilder.reflectionCompare(this, otherItem, EXCLUDED_FIELDS);
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.JSON_STYLE);
        builder.setExcludeFieldNames(EXCLUDED_FIELDS);
        return builder.toString();
    }

}
