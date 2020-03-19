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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ComponentKeys extends AlertSerializableModel implements Comparable<ComponentKeys> {
    private static final char KEY_SEPARATOR = '_';

    private final String category;
    private final String componentName;
    private final String componentValue;
    private final String subComponentName;
    private final String subComponentValue;
    private final String additionalData;

    public ComponentKeys(final String category, final String componentName, final String componentValue, final String subComponentName, final String subComponentValue, final String additionalData) {
        this.category = category;
        this.componentName = componentName;
        this.componentValue = componentValue;
        this.subComponentName = subComponentName;
        this.subComponentValue = subComponentValue;
        this.additionalData = additionalData;
    }

    public String getCategory() {
        return category;
    }

    public static String generateAdditionalDataString(Collection<LinkableItem> componentAttributes) {
        StringBuilder additionalData = new StringBuilder();
        for (LinkableItem attribute : componentAttributes) {
            if (attribute.isPartOfKey()) {
                if (additionalData.length() > 0) {
                    additionalData.append(", ");
                }
                additionalData.append(attribute.getName());
                additionalData.append(": ");
                additionalData.append(attribute.getValue());
            }
        }
        return additionalData.toString();
    }

    public String getShallowKey() {
        List<String> keyParts = List.of(category, componentName, componentValue);
        if (StringUtils.isNotBlank(subComponentName) && StringUtils.isNotBlank(subComponentValue)) {
            keyParts = List.of(category, componentName, componentValue, subComponentName, subComponentValue);
        }
        return StringUtils.join(keyParts, KEY_SEPARATOR);
    }

    // TODO come up with better keys (maybe processing key, digest key, issue tracking key, etc)

    public String getDeepKey() {
        return StringUtils.join(getShallowKey(), KEY_SEPARATOR, additionalData);
    }

    public String prettyPrint(boolean includeAdditionalData) {
        StringBuilder prettyPrintBuilder = new StringBuilder();
        prettyPrintBuilder.append(category);
        prettyPrintBuilder.append(" - ");
        prettyPrintBuilder.append(componentName);
        prettyPrintBuilder.append(": ");
        prettyPrintBuilder.append(componentValue);
        if (StringUtils.isNotBlank(subComponentName) && StringUtils.isNotBlank(subComponentValue)) {
            prettyPrintBuilder.append(", ");
            prettyPrintBuilder.append(subComponentName);
            prettyPrintBuilder.append(": ");
            prettyPrintBuilder.append(subComponentValue);
        }
        if (includeAdditionalData && StringUtils.isNotBlank(additionalData)) {
            prettyPrintBuilder.append(", ");
            prettyPrintBuilder.append(additionalData);
        }
        return prettyPrintBuilder.toString();
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (otherObject instanceof ComponentKeys) {
            ComponentKeys otherKey = (ComponentKeys) otherObject;
            return 0 == this.compareTo(otherKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getDeepKey().hashCode();
    }

    @Override
    public int compareTo(final ComponentKeys other) {
        if (null == other) {
            throw new NullPointerException("Other component key cannot be null");
        }
        return this.getDeepKey().compareTo(other.getDeepKey());
    }

}
