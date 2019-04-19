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
package com.synopsys.integration.alert.provider.blackduck.collector.item;

import java.util.SortedSet;
import java.util.TreeSet;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderContentTypes;

public class BlackDuckPolicyLinkableItem extends AlertSerializableModel {
    private final SortedSet<LinkableItem> componentData;

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
        addComponentData(new LinkableItem(BlackDuckProviderContentTypes.LABEL_COMPONENT_NAME, name, url));
    }

    public void addComponentVersionItem(final String version, final String url) {
        addComponentData(new LinkableItem(BlackDuckProviderContentTypes.LABEL_COMPONENT_VERSION_NAME, version, url));
    }

}
