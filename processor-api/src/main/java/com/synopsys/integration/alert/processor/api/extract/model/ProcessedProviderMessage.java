/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model;

import java.util.Set;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ProcessedProviderMessage<T extends ProviderMessage<T>> extends AlertSerializableModel {
    private final Set<Long> notificationIds;
    private final T providerMessage;

    public static <T extends ProviderMessage<T>> ProcessedProviderMessage<T> singleSource(Long notificationId, T providerMessage) {
        return new ProcessedProviderMessage<>(Set.of(notificationId), providerMessage);
    }

    public ProcessedProviderMessage(Set<Long> notificationIds, T providerMessage) {
        this.notificationIds = notificationIds;
        this.providerMessage = providerMessage;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }

    public T getProviderMessage() {
        return providerMessage;
    }

}
