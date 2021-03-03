/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class AlertIssueOrigin extends AlertSerializableModel {
    private final ContentKey providerContentKey;
    private final ComponentItem componentItem;

    public AlertIssueOrigin(ContentKey providerContentKey) {
        this(providerContentKey, null);
    }

    public AlertIssueOrigin(ContentKey providerContentKey, @Nullable ComponentItem componentItem) {
        this.providerContentKey = providerContentKey;
        this.componentItem = componentItem;
    }

    public ContentKey getProviderContentKey() {
        return providerContentKey;
    }

    public Optional<ComponentItem> getComponentItem() {
        return Optional.ofNullable(componentItem);
    }

}
