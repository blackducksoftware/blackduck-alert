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
package com.synopsys.integration.alert.channel.event;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.event.ContentEvent;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;

public class DistributionEvent extends ContentEvent {
    private final FieldAccessor fieldAccessor;
    private final String configId;
    private Map<Long, Long> notificationIdToAuditId;

    public DistributionEvent(final String configId, final String destination, final String createdAt, final String provider, final String formatType, final AggregateMessageContent content, final FieldAccessor fieldAccessor) {
        super(destination, createdAt, provider, formatType, content);
        this.fieldAccessor = fieldAccessor;
        this.configId = configId;
    }

    public FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    public String getConfigId() {
        return configId;
    }

    public Map<Long, Long> getNotificationIdToAuditId() {
        return notificationIdToAuditId;
    }

    public void setNotificationIdToAuditId(final Map<Long, Long> notificationIdToAuditId) {
        this.notificationIdToAuditId = notificationIdToAuditId;
    }

    public Set<Long> getAuditIds() {
        if (null != notificationIdToAuditId && !notificationIdToAuditId.isEmpty()) {
            return notificationIdToAuditId.entrySet()
                       .stream()
                       .filter(entry -> null != entry.getValue())
                       .map(entry -> entry.getValue())
                       .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
