/*
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
package com.synopsys.integration.alert.common.event;

import com.synopsys.integration.alert.common.message.model.MessageContentGroup;

public class ContentEvent extends AlertEvent {
    private static final long serialVersionUID = 8592125218004089822L;
    private final String createdAt;
    private final Long providerConfigId;
    private final String processingType;
    private final MessageContentGroup contentGroup;

    public ContentEvent(String destination, String createdAt, Long providerConfigId, String processingType, MessageContentGroup contentGroup) {
        super(destination);
        this.createdAt = createdAt;
        this.providerConfigId = providerConfigId;
        this.processingType = processingType;
        this.contentGroup = contentGroup;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getProcessingType() {
        return processingType;
    }

    public MessageContentGroup getContent() {
        return contentGroup;
    }

}
