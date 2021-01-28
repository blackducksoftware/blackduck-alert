/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class MessageContentGroup extends AlertSerializableModel {
    private final List<ProviderMessageContent> subContent;

    private LinkableItem comonProvider;
    private LinkableItem commonTopic;

    public static MessageContentGroup singleton(ProviderMessageContent message) {
        MessageContentGroup group = new MessageContentGroup();
        group.add(message);
        return group;
    }

    public MessageContentGroup() {
        this.subContent = new LinkedList<>();
        this.commonTopic = null;
    }

    public boolean applies(ProviderMessageContent message) {
        return null == commonTopic || commonTopic.getValue().equals(message.getTopic().getValue());
    }

    public void add(ProviderMessageContent message) {
        if (null == commonTopic) {
            comonProvider = message.getProvider();
            commonTopic = message.getTopic();
        } else if (!commonTopic.getValue().equals(message.getTopic().getValue())) {
            throw new IllegalArgumentException(String.format("The topic of this message did not match the group topic. Expected: %s. Actual: %s.", commonTopic.getValue(), message.getTopic().getValue()));
        }

        if (commonTopic.getUrl().isEmpty() && message.getTopic().getUrl().isPresent()) {
            commonTopic = message.getTopic();
        }

        subContent.add(message);
    }

    public void addAll(Collection<ProviderMessageContent> messages) {
        messages.forEach(this::add);
    }

    public List<ProviderMessageContent> getSubContent() {
        return subContent;
    }

    public LinkableItem getCommonProvider() {
        return comonProvider;
    }

    public LinkableItem getCommonTopic() {
        return commonTopic;
    }

    public boolean isEmpty() {
        return subContent.isEmpty() || StringUtils.isBlank(commonTopic.getLabel()) || StringUtils.isBlank(commonTopic.getValue());
    }

}
