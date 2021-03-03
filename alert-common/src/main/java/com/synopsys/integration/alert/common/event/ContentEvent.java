/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
