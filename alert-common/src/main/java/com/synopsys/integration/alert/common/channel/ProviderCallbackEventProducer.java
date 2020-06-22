package com.synopsys.integration.alert.common.channel;

import java.util.List;

import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;

public interface ProviderCallbackEventProducer {
    void sendProviderCallbackEvents(List<ProviderCallbackEvent> event);

}
