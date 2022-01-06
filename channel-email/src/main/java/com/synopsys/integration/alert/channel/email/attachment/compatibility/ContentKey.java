/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.attachment.compatibility;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;

public class ContentKey extends AlertSerializableModel {
    private static final String KEY_SEPARATOR = "_";
    private static final long serialVersionUID = 3508232762022869668L;
    private final String providerName;
    private final Long providerConfigId;
    private final String topicName;
    private final String topicValue;
    private final String subTopicName;
    private final String subTopicValue;

    private final String value;

    public ContentKey(String providerName, Long providerConfigId, String topicName, String topicValue, String subTopicName, String subTopicValue, ItemOperation action) {
        this.providerName = providerName;
        this.providerConfigId = providerConfigId;
        this.topicName = topicName;
        this.topicValue = topicValue;
        this.subTopicName = subTopicName;
        this.subTopicValue = subTopicValue;
        this.value = generateContentKey(providerName, providerConfigId, topicName, topicValue, subTopicName, subTopicValue, action);
    }

    public static final ContentKey of(String providerName, Long providerConfigId, String topicName, String topicValue, String subTopicName, String subTopicValue, ItemOperation action) {
        return new ContentKey(providerName, providerConfigId, topicName, topicValue, subTopicName, subTopicValue, action);
    }

    private String generateContentKey(String providerName, Long providerConfigId, String topicName, String topicValue, String subTopicName, String subTopicValue, ItemOperation action) {
        List<String> keyParts = new ArrayList<>(6);
        keyParts.add(providerName);
        if (null != providerConfigId) {
            keyParts.add(String.valueOf(providerConfigId));
        }
        keyParts.add(topicName);
        keyParts.add(topicValue);
        if (StringUtils.isNotBlank(subTopicName)) {
            keyParts.add(subTopicName);
            keyParts.add(subTopicValue);
        }
        if (null != action) {
            keyParts.add(action.name());
        }
        return StringUtils.join(keyParts, KEY_SEPARATOR);
    }

    public String getProviderName() {
        return providerName;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
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

    public String getValue() {
        return value;
    }

}
