/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
