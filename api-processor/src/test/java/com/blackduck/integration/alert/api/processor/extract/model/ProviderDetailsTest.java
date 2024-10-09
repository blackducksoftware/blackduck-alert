/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.extract.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class ProviderDetailsTest {
    private final Long providerConfigId = 1L;
    private final LinkableItem provider = new LinkableItem("Provider", "BlackDuck provider");

    @Test
    public void getProviderConfigIdTest() {
        ProviderDetails providerDetails = new ProviderDetails(providerConfigId, provider);
        assertEquals(providerConfigId, providerDetails.getProviderConfigId());
    }

    @Test
    public void getProviderTest() {
        ProviderDetails providerDetails = new ProviderDetails(providerConfigId, provider);
        assertEquals(provider, providerDetails.getProvider());
    }
}
