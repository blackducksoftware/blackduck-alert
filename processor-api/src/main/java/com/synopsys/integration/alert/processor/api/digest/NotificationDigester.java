package com.synopsys.integration.alert.processor.api.digest;

import java.util.List;

import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;

public interface NotificationDigester {
    List<ProviderMessage<?>> digest(List<ProviderMessage<?>> notifications);

}
