package com.synopsys.integration.alert.common.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.exception.IntegrationException;

public abstract class ProviderCallbackHandler extends MessageReceiver<ProviderCallbackEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ProviderKey providerKey;

    public ProviderCallbackHandler(Gson gson, ProviderKey providerKey) {
        super(gson, ProviderCallbackEvent.class);
        this.providerKey = providerKey;
    }

    public ProviderKey getProviderKey() {
        return providerKey;
    }

    @Override
    public void handleEvent(ProviderCallbackEvent event) {
        if (event.getDestination().equals(providerKey.getUniversalKey())) {
            try {
                performProviderCallback(event);
            } catch (IntegrationException ex) {
                logger.error("There was an error performing the callback.", ex);
            }
        } else {
            logger.warn("Received an event for provider '{}', but this provider is '{}' with key '{}'.", event.getDestination(), providerKey.getDisplayName(), providerKey.getUniversalKey());
        }
    }

    protected abstract void performProviderCallback(ProviderCallbackEvent event) throws IntegrationException;

}
