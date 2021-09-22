package com.synopsys.integration.alert.processor.api.extract.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

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
