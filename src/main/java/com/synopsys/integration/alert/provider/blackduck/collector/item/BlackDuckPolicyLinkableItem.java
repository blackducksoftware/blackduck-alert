/**
 * blackduck-alert
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
package com.synopsys.integration.alert.provider.blackduck.collector.item;

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;

// TODO refactor class
// 1. rename to BlackDuckPolicyComponentContainer
// 2. remove the SortedSet of linkable items.
public class BlackDuckPolicyLinkableItem extends AlertSerializableModel {
    private final SortedSet<LinkableItem> componentData;
    private LinkableItem componentItem;
    private LinkableItem componentVersion;
    private ComponentVersionStatus componentVersionStatus;

    public BlackDuckPolicyLinkableItem() {
        componentData = new TreeSet<>();
    }

    public SortedSet<LinkableItem> getComponentData() {
        return componentData;
    }

    public void addComponentData(final LinkableItem linkableItem) {
        componentData.add(linkableItem);
    }

    public void addComponentNameItem(final String name, final String url) {
        final LinkableItem newItem = new LinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, name, url);
        newItem.setCollapsible(false);
        this.componentItem = newItem;
        addComponentData(newItem);
    }

    public void addComponentVersionItem(final String version, final String url) {
        this.componentVersion = new LinkableItem(BlackDuckContent.LABEL_COMPONENT_VERSION_NAME, version, url);
        addComponentData(componentVersion);
    }

    public Optional<LinkableItem> getComponentItem() {
        return Optional.ofNullable(componentItem);
    }

    public Optional<LinkableItem> getComponentVersion() {
        return Optional.ofNullable(componentVersion);
    }

    public ComponentVersionStatus getComponentVersionStatus() {
        return componentVersionStatus;
    }

    public void setComponentVersionStatus(final ComponentVersionStatus componentVersionStatus) {
        this.componentVersionStatus = componentVersionStatus;
    }
}
