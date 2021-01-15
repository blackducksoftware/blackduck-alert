package com.synopsys.integration.alert.processor.api.detail;

import java.util.List;

import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;

public interface NotificationDetailer {
    NotificationDetails detail(List<ProviderMessage<?>> providerMessage);

}
