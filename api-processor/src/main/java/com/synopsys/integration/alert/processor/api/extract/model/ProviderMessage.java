/*
 * api-processor
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public abstract class ProviderMessage<T extends ProviderMessage<T>> extends AlertSerializableModel implements CombinableModel<T> {
    private final ProviderDetails providerDetails;

    public ProviderMessage(ProviderDetails providerDetails) {
        this.providerDetails = providerDetails;
    }

    public ProviderDetails getProviderDetails() {
        return providerDetails;
    }

    public LinkableItem getProvider() {
        return providerDetails.getProvider();
    }

}
