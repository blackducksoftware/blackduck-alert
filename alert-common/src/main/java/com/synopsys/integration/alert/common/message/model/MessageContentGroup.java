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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class MessageContentGroup extends AlertSerializableModel {
    private final List<ProviderMessageContent> subContent;

    @Deprecated(since = "6.5.0")
    private static final String COMMON_PROVIDER_TYPO_SERIALIZATION_NAME = "comonProvider";
    @Deprecated(since = "6.5.0")
    private static final String COMMON_PROJECT_SERIALIZATION_NAME = "commonTopic";

    @JsonProperty(COMMON_PROVIDER_TYPO_SERIALIZATION_NAME)
    @SerializedName(COMMON_PROVIDER_TYPO_SERIALIZATION_NAME)
    private LinkableItem commonProvider;
    @JsonProperty(COMMON_PROJECT_SERIALIZATION_NAME)
    @SerializedName(COMMON_PROJECT_SERIALIZATION_NAME)
    private LinkableItem commonProject;

    public static MessageContentGroup singleton(ProviderMessageContent message) {
        MessageContentGroup group = new MessageContentGroup();
        group.add(message);
        return group;
    }

    public MessageContentGroup() {
        this.subContent = new LinkedList<>();
        this.commonProject = null;
    }

    public boolean applies(ProviderMessageContent message) {
        return null == commonProject || commonProject.getValue().equals(message.getProject().getValue());
    }

    public void add(ProviderMessageContent message) {
        if (null == commonProject) {
            commonProvider = message.getProvider();
            commonProject = message.getProject();
        } else if (!commonProject.getValue().equals(message.getProject().getValue())) {
            throw new IllegalArgumentException(String.format("The project of this message content did not match the group project. Expected: %s. Actual: %s.", commonProject.getValue(), message.getProject().getValue()));
        }

        if (commonProject.getUrl().isEmpty() && message.getProject().getUrl().isPresent()) {
            commonProject = message.getProject();
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
        return commonProvider;
    }

    public LinkableItem getCommonProject() {
        return commonProject;
    }

    public boolean isEmpty() {
        return subContent.isEmpty() || StringUtils.isBlank(commonProject.getLabel()) || StringUtils.isBlank(commonProject.getValue());
    }

}
