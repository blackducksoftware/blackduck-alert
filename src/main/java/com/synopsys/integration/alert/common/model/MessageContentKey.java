/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.common.model;

import com.synopsys.integration.util.Stringable;

public final class MessageContentKey extends Stringable {
    private final String key;

    private MessageContentKey(final String key) {
        this.key = key;
    }

    public static MessageContentKey from(final String topicName, final String topicValue) {
        final String partialKey = String.format("%s_%s", topicName, topicValue);
        return new MessageContentKey(partialKey);
    }

    public static MessageContentKey from(final String topicName, final String topicValue, final String subTopicName, final String subTopicValue) {
        if (subTopicName == null || subTopicValue == null) {
            return from(topicName, topicValue);
        }
        final String fullKey = String.format("%s_%s_%s_%s", topicName, topicValue, subTopicName, subTopicValue);
        return new MessageContentKey(fullKey);
    }

    public String getKey() {
        return key;
    }
}
