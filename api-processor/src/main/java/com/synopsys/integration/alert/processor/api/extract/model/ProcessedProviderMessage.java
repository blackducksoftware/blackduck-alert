/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class ProcessedProviderMessage<T extends ProviderMessage<T>> extends AlertSerializableModel implements CombinableModel<ProcessedProviderMessage<T>> {
    private static final String[] EXCLUDED_COMPARISON_FIELDS = { "notificationIds" };
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

    @Override
    public List<ProcessedProviderMessage<T>> combine(ProcessedProviderMessage<T> otherModel) {
        List<T> combinedMessages = this.getProviderMessage().combine(otherModel.getProviderMessage());

        int combinedMessagesSize = combinedMessages.size();
        if (0 == combinedMessagesSize) {
            return List.of();
        } else if (1 == combinedMessagesSize) {
            Set<Long> combinedNotificationIds = Stream.concat(this.getNotificationIds().stream(), otherModel.getNotificationIds().stream()).collect(Collectors.toSet());
            ProcessedProviderMessage<T> combinedProcessedMessages = new ProcessedProviderMessage<>(combinedNotificationIds, combinedMessages.get(0));
            return List.of(combinedProcessedMessages);
        } else if (2 == combinedMessagesSize) {
            return List.of(
                new ProcessedProviderMessage<>(this.getNotificationIds(), combinedMessages.get(0)),
                new ProcessedProviderMessage<>(otherModel.getNotificationIds(), combinedMessages.get(1))
            );
        }
        throw new IllegalStateException("Combining models had more than two results");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, EXCLUDED_COMPARISON_FIELDS);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, EXCLUDED_COMPARISON_FIELDS);
    }
}
