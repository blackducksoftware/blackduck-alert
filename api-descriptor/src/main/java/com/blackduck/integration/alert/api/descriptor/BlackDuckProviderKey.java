package com.blackduck.integration.alert.api.descriptor;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ProviderKey;

@Component
public final class BlackDuckProviderKey extends ProviderKey {
    public BlackDuckProviderKey() {
        super("provider_blackduck", "Black Duck");
    }

}
