/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.extract.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public abstract class ProviderMessage<T extends ProviderMessage<T>> extends AlertSerializableModel implements CombinableModel<T> {
    private final ProviderDetails providerDetails;

    protected ProviderMessage(ProviderDetails providerDetails) {
        this.providerDetails = providerDetails;
    }

    public ProviderDetails getProviderDetails() {
        return providerDetails;
    }

    public LinkableItem getProvider() {
        return providerDetails.getProvider();
    }

}
