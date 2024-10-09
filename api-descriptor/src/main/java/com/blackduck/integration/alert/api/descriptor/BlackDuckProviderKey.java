/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.descriptor;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ProviderKey;

@Component
public final class BlackDuckProviderKey extends ProviderKey {
    public BlackDuckProviderKey() {
        super("provider_blackduck", "Black Duck");
    }

}
