/**
 * channel
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
package com.synopsys.integration.alert.channel.azure.boards;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueSearchProperties;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class AzureBoardsSearchProperties extends AlertSerializableModel implements IssueSearchProperties {
    private final String providerKey;
    private final String topicKey;
    private final String subTopicKey;
    private final String categoryKey;
    private final String componentKey;
    private final String subComponentKey;
    private final String additionalInfoKey;

    public AzureBoardsSearchProperties(String providerKey, String topicKey, @Nullable String subTopicKey, @Nullable String categoryKey, @Nullable String componentKey, @Nullable String subComponentKey, @Nullable String additionalInfoKey) {
        this.providerKey = providerKey;
        this.topicKey = topicKey;
        this.subTopicKey = subTopicKey;
        this.categoryKey = categoryKey;
        this.componentKey = componentKey;
        this.subComponentKey = subComponentKey;
        this.additionalInfoKey = additionalInfoKey;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public String getTopicKey() {
        return topicKey;
    }

    public Optional<String> getSubTopicKey() {
        return Optional.ofNullable(subTopicKey);
    }

    public Optional<String> getCategoryKey() {
        return Optional.ofNullable(categoryKey);
    }

    public Optional<String> getComponentKey() {
        return Optional.ofNullable(componentKey);
    }

    public Optional<String> getSubComponentKey() {
        return Optional.ofNullable(subComponentKey);
    }

    public Optional<String> getAdditionalInfoKey() {
        return Optional.ofNullable(additionalInfoKey);
    }

}
