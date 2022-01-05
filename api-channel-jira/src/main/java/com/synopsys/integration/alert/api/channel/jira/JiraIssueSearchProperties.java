/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira;

import java.io.Serializable;

import com.synopsys.integration.util.Stringable;

public class JiraIssueSearchProperties extends Stringable implements Serializable {
    private String provider;
    private String providerUrl;
    private String topicName;
    private String topicValue;
    private String subTopicName;
    private String subTopicValue;
    private String category;
    private String componentName;
    private String componentValue;
    private String subComponentName;
    private String subComponentValue;
    private String additionalKey;

    public JiraIssueSearchProperties() {
        // For serialization
    }

    public JiraIssueSearchProperties(
        String provider,
        String providerUrl,
        String topicName,
        String topicValue,
        String subTopicName,
        String subTopicValue,
        String category,
        String componentName,
        String componentValue,
        String subComponentName,
        String subComponentValue,
        String additionalKey
    ) {
        this.provider = provider;
        this.providerUrl = providerUrl;
        this.topicName = topicName;
        this.topicValue = topicValue;
        this.subTopicName = subTopicName;
        this.subTopicValue = subTopicValue;
        this.category = category;
        this.componentName = componentName;
        this.componentValue = componentValue;
        this.subComponentName = subComponentName;
        this.subComponentValue = subComponentValue;
        this.additionalKey = additionalKey;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getTopicValue() {
        return topicValue;
    }

    public String getSubTopicName() {
        return subTopicName;
    }

    public String getSubTopicValue() {
        return subTopicValue;
    }

    public String getCategory() {
        return category;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentValue() {
        return componentValue;
    }

    public String getSubComponentName() {
        return subComponentName;
    }

    public String getSubComponentValue() {
        return subComponentValue;
    }

    public String getAdditionalKey() {
        return additionalKey;
    }

}
