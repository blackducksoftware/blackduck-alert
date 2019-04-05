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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class BlackDuckPolicyLinkableItem extends AlertSerializableModel {
    private SortedSet<LinkableItem> linkableItems;
    private Set<String> policyUrls;

    public BlackDuckPolicyLinkableItem() {
        linkableItems = new TreeSet<>();
        policyUrls = new HashSet<>();
    }

    public BlackDuckPolicyLinkableItem(final SortedSet<LinkableItem> linkableItems, final Set<String> policyUrls) {
        this.linkableItems = linkableItems;
        this.policyUrls = policyUrls;
    }

    public SortedSet<LinkableItem> getLinkableItems() {
        return linkableItems;
    }

    public void setLinkableItems(final SortedSet<LinkableItem> linkableItems) {
        this.linkableItems = linkableItems;
    }

    public void addLinkableItem(final LinkableItem linkableItem) {
        linkableItems.add(linkableItem);
    }

    public Set<String> getPolicyUrls() {
        return policyUrls;
    }

    public void setPolicyUrls(final Set<String> policyUrls) {
        this.policyUrls = policyUrls;
    }

    public void addPolicyUrl(final String policyUrl) {
        policyUrls.add(policyUrl);
    }

    public Boolean containsPolicyUrl(final String policy) {
        return policyUrls.contains(policy);
    }
}
