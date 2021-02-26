/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ProviderDetails extends AlertSerializableModel {
    private final Long providerConfigId;
    private final LinkableItem provider;

    public ProviderDetails(Long providerConfigId, LinkableItem provider) {
        this.providerConfigId = providerConfigId;
        this.provider = provider;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public LinkableItem getProvider() {
        return provider;
    }

}
