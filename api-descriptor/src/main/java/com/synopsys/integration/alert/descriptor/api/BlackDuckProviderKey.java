/*
 * api-descriptor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.descriptor.api;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

@Component
public final class BlackDuckProviderKey extends ProviderKey {
    public BlackDuckProviderKey() {
        super("provider_blackduck", "Black Duck");
    }

}
