/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

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
