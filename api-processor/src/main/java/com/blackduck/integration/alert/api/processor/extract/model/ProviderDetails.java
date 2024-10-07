/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.extract.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

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
